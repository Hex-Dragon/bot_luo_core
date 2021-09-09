package bot_luo_core.bot

import bot_luo_core.cli.CmdHandler
import bot_luo_core.data.DataObj
import bot_luo_core.util.JsonWorker
import bot_luo_core.util.Logger
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
import org.apache.logging.log4j.Level

/**
 * # 多机器人管理类
 */
object MultiBotHandler {
    private const val FILE_PATH = "data/bots.json"

    /**
     * ## 所有机器人实例列表
     */
    private val bots get() =  Bot.instances

    /**
     * ## 机器人与群组映射关系
     */
    private val groupMap = HashMap<Long,ArrayList<Bot>>()

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

//            //捕获群员退群（踢出）事件
//            bot.eventChannel.subscribeAlways<MemberLeaveEvent> {
//                if (bot.isMainBot(this.groupId)) {
//                    Logger.receive(this)
//                    if (this is MemberLeaveEvent.Kick)
//                        WarnCMD.closeRec(Users.readUser(member.id),operator?.id, Time.time())
//                    else
//                        WarnCMD.closeRec(Users.readUser(member.id),null, Time.time())
//                }
//            }
//
//            //捕获加群事件
//            bot.eventChannel.subscribeAlways<MemberJoinEvent> {
//                if (bot.isMainBot(this.groupId)) {
//                    Logger.receive(this)
//                    if (group.id == Tags.readGroupTag("#rsc")[0].id) return@subscribeAlways
//                    //查询Warn记录，若大于3分则在#clip群发送警告
//                    val user = Users.readUser(this.member.id)
//                    if (WarnCMD.getScore(user) >= 3) {
//                        val clipId = Tags.readGroupTag("#clip")[0]
//                        sendGroupMessage("死人 ${member.id} 想在 ${this.group.name}(${this.group.id}) 复活", clipId.id)
//                        val env = CmdEnv(null, CmdEnvType.GROUP, Time.time(), clipId, null, User.virtualUser(), CmdParser(StringReader("")))
//                        WarnCMD(env).get(user,null)
//                    }
//                }
//            }
//
//            //捕获加群申请事件
//            bot.eventChannel.subscribeAlways<MemberJoinRequestEvent> {
//                if (bot.isMainBot(this.groupId)) {
//                    Logger.receive(this)
//                    val group = Groups.readGroup(this.groupId)
//                    if (group.getCmdStateBoolean(GuardCMD::class.getName(),"auto")) {
//                        GuardCMD.judge(this)
//                    }
//                }
//            }
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

        GlobalEventChannel.subscribeAlways<MessageEvent>(priority = EventPriority.NORMAL) {
            if (this.sender is AnonymousMember) return@subscribeAlways
            //TODO 解除群锁
            if (this is GroupAwareMessageEvent && group.id != 565056329L) return@subscribeAlways
            if (this is MessageSyncEvent) return@subscribeAlways
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
//        GlobalEventChannel.subscribeAlways<MessagePreSendEvent> {
//            this.cancel()   //TODO 解除张口结舌之术
//        }

        Runtime.getRuntime().addShutdownHook(Thread{ saveAll() })
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
        runBlocking {
            DataObj.savingJobs.values.forEach { it.start(); it.join() }
            DataObj.savingJobs.keys.forEach { it.cancelAndJoin() }
        }
    }

}