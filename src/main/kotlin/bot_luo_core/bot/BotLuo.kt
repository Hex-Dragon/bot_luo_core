package bot_luo_core.bot

import bot_luo_core.cli.CmdHandler
import bot_luo_core.data.Data
import bot_luo_core.util.JsonWorker
import bot_luo_core.util.Logger
import bot_luo_core.util.Time
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.containsFriend
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.LoggerAdapters
import net.mamoe.mirai.utils.MiraiLogger
import org.apache.logging.log4j.Level
import kotlin.jvm.Throws

/**
 * # 罗伯特
 */
object BotLuo {
    private const val FILE_PATH = "data/bots.json"

    /**
     * ## 罗伯特开机时间
     */
    val startAt = Time.time()

    /**
     * ## 所有机器人实例列表
     */
    private val bots get() =  Bot.instances

    /**
     * ## 机器人与群组映射关系
     */
    private val groupMap = HashMap<Long,ArrayList<Bot>>()

    init {
        LoggerAdapters.useLog4j2()
        Runtime.getRuntime().addShutdownHook(Thread{ saveAll() })
    }

    /**
     * ## 登录所有机器人账号
     *
     * 登录所有`bot.json`中的账号并建立群组关系，只能调用一次
     */
    suspend fun loginAll() {
        val accounts = JsonWorker.readJson<ArrayList<BotAccount>>(FILE_PATH)?: throw Exception("文件不存在 $FILE_PATH")
        for (a in accounts) {
            if (a.working == false) continue
            val bot = BotFactory.newBot(a.id,a.psw) {
                a.device?.let { fileBasedDeviceInfo("data/$it") }
                protocol = BotConfiguration.MiraiProtocol.ANDROID_PAD
            }
            bot.login()
        }

        buildGroupMap()

        GlobalEventChannel.subscribeAlways<BotOfflineEvent> {
            Logger.log(this, Level.INFO)
            buildGroupMap()
        }
        GlobalEventChannel.subscribeAlways<BotOnlineEvent> {
            Logger.log(this, Level.INFO)
            buildGroupMap()
        }
        GlobalEventChannel.subscribeAlways<BotJoinGroupEvent> {
            Logger.log(this, Level.INFO)
            buildGroupMap()
        }
        GlobalEventChannel.subscribeAlways<BotLeaveEvent> {
            Logger.log(this, Level.INFO)
            buildGroupMap()
        }
        GlobalEventChannel.subscribeAlways<BotGroupPermissionChangeEvent> {
            Logger.log(this, Level.INFO)
            buildGroupMap()
        }
        GlobalEventChannel.subscribeAlways<BotMuteEvent> {
            Logger.log(this, Level.INFO)
            buildGroupMap()
        }
        GlobalEventChannel.subscribeAlways<BotUnmuteEvent> {
            Logger.log(this, Level.INFO)
            buildGroupMap()
        }
        GlobalEventChannel.subscribeAlways<MemberJoinRequestEvent> {
            Logger.log(this, Level.INFO)
        }
        GlobalEventChannel.subscribeAlways<MemberJoinEvent> {
            Logger.log(this, Level.INFO)
        }
        GlobalEventChannel.subscribeAlways<MemberLeaveEvent> {
            Logger.log(this, Level.INFO)
        }

        GlobalEventChannel.subscribeAlways<MessageEvent>(priority = EventPriority.NORMAL) {
            if (this.sender is AnonymousMember) return@subscribeAlways
            if (this is MessageSyncEvent) return@subscribeAlways
//            if (this !is GroupAwareMessageEvent || this.group.id != 565056329L) return@subscribeAlways
            if (this is GroupAwareMessageEvent && !bot.isMainBotOf(this.group.id)) return@subscribeAlways
            CmdHandler.call(this)
            if (this.isIntercepted) Logger.log(this, Level.INFO) else Logger.log(this, Level.DEBUG)
        }
        GlobalEventChannel.subscribeAlways<GroupMessageSyncEvent>(priority = EventPriority.NORMAL) {
            CmdHandler.call(this)
            if (this.isIntercepted) Logger.log(this, Level.INFO) else Logger.log(this, Level.DEBUG)
        }
        GlobalEventChannel.subscribeAlways<VirtualMessageEvent>(priority = EventPriority.NORMAL) {
            CmdHandler.call(this)
            if (this.isIntercepted) Logger.log(this, Level.INFO) else Logger.log(this, Level.DEBUG)
        }
    }

    /**
     * ## 重新建立群组关系
     *
     * 当任意机器人发生加群，退群，群权限变动，掉线和重新登陆时调用
     *
     * 主bot（下标为0）应为所有伯特中群权限最大者
     */
    private fun buildGroupMap() {
        groupMap.clear()
        for (bot in bots) {
            if (!bot.isOnline) continue
            for (g in bot.groups) {
                if (groupMap[g.id] == null)
                    groupMap[g.id] = arrayListOf(bot)
                else {
                    groupMap[g.id]!!.add(bot)
                }
            }
        }
        for ((g,b) in groupMap) {
            b.sortBy { it.getGroup(g)!!.botPermission }
            b.reverse()
        }
    }

    /**
     * ## 是否为该群组的主Bot
     *
     * 只有主Bot负责接收该群的事件，避免重复处理
     */
    fun Bot.isMainBotOf(groupId: Long) = this == groupMap[groupId]?.get(0)

    /**
     * ## 通过群号获取[mirai群组][Group]
     *
     * 此方法获取的群组所关联的Bot是不确定的
     *
     * @return mirai群组，失败返回null
     */
    fun findGroupOrNull(id: Long): Group? {
        for (b in bots) b.getGroup(id)?.let { return it }
        return null
    }

    /**
     * ## 通过群号获取[mirai用户][User]
     *
     * 此方法获取的群组所关联的Bot是不确定的
     *
     * @return mirai用户，失败返回null
     */
    fun findUserOrNull(id: Long): User? {
        for (b in bots) b.getFriend(id)?.let { return it }
        for (b in bots) b.getStranger(id)?.let { return it }
        return null
    }

    /**
     * ## 获取Mirai联系对象
     *
     * 根据[bot联系对象][BotContact]获取[Mirai联系对象][Contact]
     *
     * [contact]的type需为[BotContactType.Single]或[BotContactType.Group]
     */
    fun getMiraiContact(contact: BotContact): Contact? { with(contact) {
        if (bot == null) bot = getContactableBots(this).randomOrNull()
        return when (contactType) {
            BotContactType.Group -> bot?.getGroup(id)
            BotContactType.Single -> bot?.getFriend(id)?: bot?.getStranger(id)
        }
    } }

    /**
     * ## 根据[bot联系对象][BotContact]获取可联系bot列表
     */
    fun getContactableBots(contact: BotContact): List<Bot> { with(contact) {
        return when (contactType) {
            BotContactType.Group -> bots.filter { it.getGroup(contact.id)?.isBotMuted?.not() ?: false }
            BotContactType.Single -> bots.filter { it.containsFriend(id) || it.strangers.contains(id) }
        }
    } }

    /**
     * ## 根据id获取bot，可能返回`null`
     */
    fun getBotOrNull(id: Long): Bot? = bots.firstOrNull{ it.id == id }

    /**
     * ## 捕获下一条相同环境相同用户的消息
     *
     * 若群组为0表示为私聊环境
     *
     * @param filter 过滤消息直到获取到符合要求的消息（返回`true`）
     */
    @Throws(TimeoutCancellationException::class)
    suspend fun catchNextMessage(userId: Long, groupId: Long, timeoutMillis: Long = -1L, filter: (msg: MessageChain)->Boolean = {true}): MessageChain {
        return syncFromEvent<Event, MessageChain>(timeoutMillis, EventPriority.MONITOR) { event ->
            return@syncFromEvent when (event) {
                is VirtualMessageEvent -> if (event.user.id == userId && event.group.id == groupId) {
                    if (filter(event.message)) {
                        Logger.log(this, Level.INFO)
                        event.message
                    } else {
                        Logger.log(this, Level.DEBUG)
                        null
                    }
                } else null
                is GroupMessageEvent -> if (event.sender.id == userId && event.group.id == groupId) {
                    if (filter(event.message)) {
                        Logger.log(this, Level.INFO)
                        event.message
                    } else {
                        Logger.log(this, Level.DEBUG)
                        null
                    }
                } else null
                is UserMessageEvent -> if (event.sender.id == userId && 0L == groupId) {
                    if (filter(event.message)) {
                        Logger.log(this, Level.INFO)
                        event.message
                    } else {
                        Logger.log(this, Level.DEBUG)
                        null
                    }
                } else null
                else -> null
            }
        }
    }

    fun saveAll() {
        Data.savingJobs.values.forEach { it.invoke() }
        Data.savingJobs.clear()
    }

}