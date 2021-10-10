package bot_luo_core.cli.commands

import bot_luo_core.cli.Cmd
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdPermissionLevel
import bot_luo_core.cli.CmdReceipt
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.handlers.GreedyMessageArgHandler
import bot_luo_core.cli.handlers.TimeArgHandler
import bot_luo_core.cli.handlers.TimeSpanArgHandler
import bot_luo_core.data.Schedule
import bot_luo_core.data.ScheduleEvent
import bot_luo_core.data.withLockedAccessing
import bot_luo_core.util.GSON
import bot_luo_core.util.Logger.sendMessageWithLog
import bot_luo_core.util.ResourceManager.deleteResourceIfNeeded
import bot_luo_core.util.ResourceManager.downloadResource
import bot_luo_core.util.ResourceManager.uploadResource
import bot_luo_core.util.TableBuilder
import bot_luo_core.util.Text.limitMid
import bot_luo_core.util.Text.miraiCodeContent
import bot_luo_core.util.Time
import bot_luo_core.util.Time.relativeToE
import com.github.salomonbrys.kotson.typeToken
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.MessageChain

@Command(
    name = "schedule",
    display = "时刻表",
    alias = ["sch"],
    usage = "管理罗伯特时刻表",
    notice = [
        "添加时命令不做检查",
        "执行时刻仅精确到分钟",
        "周期至少为1分钟，为0则单次执行"
    ]
)
class ScheduleCmd(context: CmdContext) : Cmd(context) {

    /*  ========================  list  ========================  */

    @Method(name = "", alias = ["list","l"], pmsLevel = CmdPermissionLevel.OP)
    suspend fun list (): CmdReceipt {
        val table = TableBuilder(4)
        table.th("时刻表 ——")
        table.tr("序号").td("时刻").td("周期").td("命令").br()
        withLockedAccessing(Schedule) {
            for (i in Schedule.element) {
                val item = GSON.fromJson<ScheduleEvent>(i, typeToken<ScheduleEvent>())
                table.tr(item.id).td(item.time relativeToE context.time).td(Time.formatSpan(item.cycle)).td(item.cmd.miraiCodeContent.limitMid(40))
            }
        }
        context.print(table.toString())
        return SUCCESS
    }

    /*  ========================  get  ========================  */

    @Method(name = "get", alias = ["g"], pmsLevel = CmdPermissionLevel.OP)
    suspend fun get(
        @Argument(name = "序号")
        id: Int
    ): CmdReceipt {
        withLockedAccessing(Schedule) {
            val event = Schedule[id]
            return if (event == null) {
                context.print("不存在序号为 $id 的事件")
                FATAL
            } else {
                context.withBackendBot { contact ->
                    event.cmd = event.cmd.deserializeMiraiCode().uploadResource(contact).serializeToMiraiCode()
                    val table = TableBuilder(4)
                    table.th("时刻表 —— 事件($id)").br()
                    genEventInTable(table, event)
                    contact.sendMessageWithLog(table.toMessage())
                }
                SUCCESS
            }
        }
    }

    /*  ========================  add  ========================  */

    @Method(name = "add", alias = ["a"], pmsLevel = CmdPermissionLevel.OP)
    suspend fun add(
        @Argument(name = "时刻", handler = TimeArgHandler::class)
        time: Long,
        @Argument(name = "周期", handler = TimeSpanArgHandler::class)
        cycle: Long,
        @Argument(name = "命令", handler = GreedyMessageArgHandler::class)
        cmd: MessageChain
    ): CmdReceipt {
        withLockedAccessing(Schedule) {
            val newId = Schedule.newId()
            cmd.downloadResource()
            val event = ScheduleEvent (
                newId,
                context.user.id,
                context.group.id,
                time,
                cycle,
                cmd.serializeToMiraiCode()
                    )
            Schedule.addObj(event)
            val table = TableBuilder(4)
            table.th("时刻表添加 —— 事件($newId)").br()
            genEventInTable(table, event)
            context.print(table.toMessage())
            return SUCCESS
        }
    }

    /*  ========================  remove  ========================  */

    @Method(name = "remove", alias = ["r"], pmsLevel = CmdPermissionLevel.OP)
    suspend fun remove(
        @Argument(name = "序号")
        id: Int
    ): CmdReceipt {
        withLockedAccessing(Schedule) {
            val event = Schedule.removeId(id)
            if (event == null) {
                context.print("不存在序号为 $id 的事件")
                return FATAL
            }
            val table = TableBuilder(4)
            table.th("时刻表移除 —— 事件($id)").br()
            context.withBackendBot { contact ->
                event.cmd = event.cmd.deserializeMiraiCode().uploadResource(contact).serializeToMiraiCode()
                genEventInTable(table, event)
                contact.sendMessageWithLog(table.toMessage())
            }
            return SUCCESS
        }
    }

    /*  ========================  modify  ========================  */

    @Method(name = "modify", alias = ["m"], pmsLevel = CmdPermissionLevel.OP)
    suspend fun modifyTime(
        @Argument(name = "序号")
        id: Int,
        @Suppress("UNUSED_PARAMETER")
        @Argument(name = "time", literal = true)
        ignore: Any,
        @Argument(name = "时刻", handler = TimeArgHandler::class)
        time: Long
    ): CmdReceipt {
        withLockedAccessing(Schedule) {
            val event = Schedule[id]
            if (event == null) {
                context.print("不存在序号为 $id 的事件")
                return FATAL
            }
            val table = TableBuilder(4)
            table.th("时刻表修改 —— 事件($id)[时刻]").br()
            table.tr(Time.format(event.time)).td("->").td(Time.format(time))
            event.time = time
            event.user = context.user.id
            event.group = context.group.id
            context.withBackendBot { contact ->
                event.cmd.deserializeMiraiCode().uploadResource(contact)
                genEventInTable(table, event)
                contact.sendMessageWithLog(table.toMessage())
            }
            Schedule.set(event)
            return SUCCESS
        }
    }

    @Method(name = "modify", alias = ["m"], pmsLevel = CmdPermissionLevel.OP)
    suspend fun modifyCycle(
        @Argument(name = "序号")
        id: Int,
        @Suppress("UNUSED_PARAMETER")
        @Argument(name = "cycle", literal = true)
        ignore: Any,
        @Argument(name = "周期", handler = TimeSpanArgHandler::class)
        cycle: Long
    ): CmdReceipt {
        withLockedAccessing(Schedule) {
            val event = Schedule[id]
            if (event == null) {
                context.print("不存在序号为 $id 的事件")
                return FATAL
            }
            val table = TableBuilder(4)
            table.th("时刻表修改 —— 事件($id)[周期]").br()
            table.tr(Time.formatSpan(event.cycle)).td("->").td(Time.formatSpan(cycle))
            event.cycle = cycle
            event.user = context.user.id
            event.group = context.group.id
            context.withBackendBot { contact ->
                event.cmd.deserializeMiraiCode().uploadResource(contact)
                genEventInTable(table, event)
                contact.sendMessageWithLog(table.toMessage())
            }
            Schedule.set(event)
            return SUCCESS
        }
    }

    @Method(name = "modify", alias = ["m"], pmsLevel = CmdPermissionLevel.OP)
    suspend fun modifyCmd(
        @Argument(name = "序号")
        id: Int,
        @Suppress("UNUSED_PARAMETER")
        @Argument(name = "cmd", literal = true)
        ignore: Any,
        @Argument(name = "命令", handler = GreedyMessageArgHandler::class)
        cmd: MessageChain
    ): CmdReceipt {
        withLockedAccessing(Schedule) {
            val event = Schedule[id]
            if (event == null) {
                context.print("不存在序号为 $id 的事件")
                return FATAL
            }
            val table = TableBuilder(4)
            table.th("时刻表修改 —— 事件($id)[时刻]").br()
            event.user = context.user.id
            event.group = context.group.id
            cmd.downloadResource()
            context.withBackendBot { contact ->
                val cmdRaw = event.cmd.deserializeMiraiCode()
                val cmdOld = cmdRaw.uploadResource(contact)
                table.p(cmdOld).br().p("->").br().p(cmd)
                event.cmd = cmd.serializeToMiraiCode()
                genEventInTable(table, event)
                contact.sendMessageWithLog(table.toMessage())
                cmdRaw.deleteResourceIfNeeded()
            }
            return SUCCESS
        }
    }

    fun genEventInTable(table: TableBuilder, event: ScheduleEvent) {
        table.tr("群组：").td(event.group)
        table.tr("用户：").td(event.user)
        table.tr("时刻：").td(Time.format(event.time) + "  " + (event.time relativeToE context.time))
        table.tr("周期：").td(Time.formatSpan(event.cycle))
        table.p("命令：").sp().p(event.cmd.deserializeMiraiCode())
    }
}