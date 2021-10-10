package bot_luo_core.data

import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdHandler
import bot_luo_core.cli.MessageReader
import bot_luo_core.data.Config.CLOCK_CYCLE
import bot_luo_core.util.GSON
import bot_luo_core.util.Logger
import bot_luo_core.util.ResourceManager.deleteResourceIfNeeded
import bot_luo_core.util.Time
import com.github.salomonbrys.kotson.typeToken
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import org.apache.logging.log4j.Level

object Schedule: DataArray("data/schedule.json", 10000, false) {

    internal suspend fun mainClock() {
        while (true) {
            try {
                val time = Time.time()
                val contexts = ArrayList<CmdContext>()
                val jobs = ArrayList<Job>()
                withLockedAccessing(this) {
                    val modified = ArrayList<ScheduleEvent>()
                    for (i in element) {
                        val event = GSON.fromJson<ScheduleEvent>(i, typeToken<ScheduleEvent>())
                        if (event.time > time) continue
                        contexts.add(
                            CmdContext(
                                MessageReader(event.cmd.deserializeMiraiCode()),
                                Users.readUser(event.user),
                                Groups.readGroup(event.group),
                                event.time
                            )
                        )
                        event.time += event.cycle
                        modified.add(event)
                        Logger.sysLog(Level.DEBUG, "<SCHEDULE> [U:(${event.user})][G:(${event.group})] -> ${event.cmd}")
                    }
                    for (i in modified) if (i.cycle <= 0) {
                        removeId(i.id)?.apply {
                            cmd.deserializeMiraiCode().deleteResourceIfNeeded()
                        }
                    } else set(i)
                }
                for (c in contexts) jobs.add(CmdHandler.call(c))
                delay(CLOCK_CYCLE)
                for (job in jobs) if (job.isActive) job.cancel(CancellationException("Schedule wait time out"))
            } catch (e: Throwable) {
                Logger.sysLog(Level.WARN, "<SCHEDULE> ${e.message}")
                Logger.sysLog(Level.WARN, e.stackTraceToString())
            }
        }
    }

    operator fun get(id: Int): ScheduleEvent? {
        return element.find { (it as JsonObject).get("id").asInt == id }?.let { GSON.fromJson(it, typeToken<ScheduleEvent>()) }
    }

    fun newId() = (1..element.size()).find { get(it) == null } ?: element.size().inc()

    fun set(event: ScheduleEvent) {
        removeId(event.id)
        addObj(event)
    }

    fun removeId(id: Int): ScheduleEvent? {
        val index = element.indexOfFirst { (it as JsonObject)["id"].asInt == id }
        if (index == -1) return null
        return removeObj(index).let { GSON.fromJson(it, typeToken<ScheduleEvent>()) }
    }
}

data class ScheduleEvent(
    val id: Int,
    var user: Long,
    var group: Long,
    var time: Long,
    var cycle: Long,
    var cmd: String
)