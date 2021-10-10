package bot_luo_core.util

import bot_luo_core.bot.VirtualMessageEvent
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable
import bot_luo_core.data.Group
import bot_luo_core.data.User
import bot_luo_core.util.Text.escapeJson
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.message.data.toPlainText
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager

object Logger {

    private val sysLogger = LogManager.getLogger("Sys")
    private val msgLogger = LogManager.getLogger("Msg")
    private val eventLogger = LogManager.getLogger("Event")
    private val cliLogger = LogManager.getLogger("Cli")

    /*  ========================  SYS  ========================  */

    fun sysLog(level: Level, msg: String) = sysLogger.log(level, msg)

    /*  ========================  MSG  ========================  */

    fun msgLog(level: Level, msg: String) = msgLogger.log(level, msg)

    fun log(e: MessageEvent, level: Level = Level.INFO) = when(e) {
        is GroupMessageEvent -> log(e, level)
        is GroupTempMessageEvent -> log(e, level)
        is UserMessageEvent -> log(e, level)
        else -> msgLog(level, "[U:${e.sender.nameCardOrNick}(${e.sender.id})] -> ${e.message.serializeToMiraiCode()}")
    }

    fun log(e: GroupMessageEvent, level: Level = Level.INFO) =
        msgLog(level, "[U:${e.sender.nameCardOrNick}(${e.sender.id})][G:${e.group.name}(${e.group.id})] -> ${e.message.serializeToMiraiCode()}")

    fun log(e: GroupTempMessageEvent, level: Level = Level.INFO) =
        msgLog(level, "[U:${e.sender.nameCardOrNick}(${e.sender.id})][B:${e.bot.nick}(${e.bot.id})] -> ${e.message.serializeToMiraiCode()}")

    fun log(e: UserMessageEvent, level: Level = Level.INFO) =
        msgLog(level, "[U:${e.sender.nameCardOrNick}(${e.sender.id})][B:${e.bot.nick}(${e.bot.id})] -> ${e.message.serializeToMiraiCode()}")

    fun log(e: VirtualMessageEvent, level: Level = Level.INFO) =
        eventLog(level, "-> <VMSG> [U:${e.user.name}(${e.user.id})][G:${e.group.name}(${e.group.id})] -> ${e.message.serializeToMiraiCode()}")

    fun msgLog(m: MessageChain, u: User?, g:Group?, level: Level = Level.INFO) = when {
        g == null && u != null -> msgLog(level, "[U:${u.name}(${u.id})] <- ${m.serializeToMiraiCode()}")
        u == null && g != null -> msgLog(level, "[G:${g.name}(${g.id})] <- ${m.serializeToMiraiCode()}")
        g == null || u == null -> {}
        !g.virtual -> msgLog(level, "[U:${u.name}(${u.id})][G:${g.name}(${g.id})] <- ${m.serializeToMiraiCode()}")
        !u.virtual -> msgLog(level, "[U:${u.name}(${u.id})] <- ${m.serializeToMiraiCode()}")
        else -> msgLog(level, "|<- ${m.serializeToMiraiCode()}")
    }

    suspend inline fun Contact.sendMessageWithLog(str: String, level: Level = Level.INFO): MessageReceipt<Contact>? = sendMessageWithLog(str.toPlainText(), level)
    suspend inline fun Contact.sendMessageWithLog(msg: Message, level: Level = Level.INFO): MessageReceipt<Contact>? = sendMessageWithLog(msg.toMessageChain(), level)
    suspend inline fun Contact.sendMessageWithLog(msg: MessageChain, level: Level = Level.INFO): MessageReceipt<Contact>? {
        when (this) {
            is net.mamoe.mirai.contact.Group -> msgLog(level, "[G:${this.name}(${this.id})] <- ${msg.serializeToMiraiCode()}")
            is net.mamoe.mirai.contact.User -> msgLog(level, "[U:${this.nameCardOrNick}(${this.id})] <- ${msg.serializeToMiraiCode()}")
            else -> msgLog(Level.WARN, "[UNK] <- ${msg.serializeToMiraiCode()}")
        }
        return try {
            this.sendMessage(msg)
        } catch (e: EventCancelledException) {
            null
        }
    }

    /*  ========================  EVENT  ========================  */

    fun eventLog(level: Level, msg: String) = eventLogger.log(level, msg)

    fun log(e: Event, level: Level = Level.INFO) =
        eventLog(Level.WARN, "-> <UNK> ${e::class.simpleName}")

    fun log(e: BotJoinGroupEvent, level: Level = Level.INFO) =
        eventLog(level, "[B:${e.bot.nick}(${e.bot.id})][G:${e.group.name}(${e.group.id})] -> <BOT JOIN>")

    fun log(e: BotLeaveEvent, level: Level = Level.INFO) =
        eventLog(level, "[B:${e.bot.nick}(${e.bot.id})][G:${e.group.name}(${e.group.id})] -> <BOT LEAVE>")

    fun log(e: BotGroupPermissionChangeEvent, level: Level = Level.INFO) =
        eventLog(level, "[B:${e.bot.nick}(${e.bot.id})][G:${e.group.name}(${e.group.id})] -> <BOT PMS> from ${e.origin} to ${e.new}")

    fun log(e: BotOnlineEvent, level: Level = Level.INFO) =
        eventLog(level, "[B:${e.bot.nick}(${e.bot.id})] -> <BOT ONLINE>")

    fun log(e: BotOfflineEvent, level: Level = Level.INFO) =
        eventLog(level, "[B:${e.bot.nick}(${e.bot.id})] -> <BOT OFFLINE>")

    fun log(e: BotMuteEvent, level: Level = Level.INFO) =
        eventLog(level, "[B:${e.bot.nick}(${e.bot.id})][G:${e.group.name}(${e.group.id})] -> <BOT MUTE> ${e.durationSeconds}s, by ${e.operator.nick}(${e.operator.id})")

    fun log(e: BotUnmuteEvent, level: Level = Level.INFO) =
        eventLog(level, "[B:${e.bot.nick}(${e.bot.id})][G:${e.group.name}(${e.group.id})] -> <BOT UNMUTE> by ${e.operator.nick}(${e.operator.id})")

    fun log(e: MemberJoinRequestEvent, level: Level = Level.INFO) = if(e.invitor == null) {
        eventLog(level, "[U:${e.fromNick}(${e.fromId})][G:${e.group?.name}(${e.group?.id})] -> <JOIN REQ> ${e.message.escapeJson()}")
    } else {
        eventLog(level, "[U:${e.fromNick}(${e.fromId})][G:${e.group?.name}(${e.group?.id})] -> <JOIN REQ> invite by ${e.invitor?.nick}(${e.invitor?.id})")
    }

    fun log(e: MemberJoinEvent, level: Level = Level.INFO) = when(e) {
        is MemberJoinEvent.Invite -> eventLog(level, "[U:${e.member.nameCardOrNick}(${e.member.id})][G:${e.group.name}(${e.group.id})] -> <JOIN> invite by ${e.invitor.nick}(${e.invitor.id})")
        is MemberJoinEvent.Active -> eventLog(level, "[U:${e.member.nameCardOrNick}(${e.member.id})][G:${e.group.name}(${e.group.id})] -> <JOIN> active")
        else -> eventLog(level, "[U:${e.member.nameCardOrNick}(${e.member.id})][G:${e.group.name}(${e.group.id})] -> <JOIN> ${e::class.simpleName}")
    }

    fun log(e: MemberLeaveEvent, level: Level = Level.INFO) = when(e) {
        is MemberLeaveEvent.Kick -> eventLog(level, "[U:${e.member.nameCardOrNick}(${e.member.id})][G:${e.group.name}(${e.group.id})] -> <LEAVE> kick by ${e.operatorOrBot.nick}(${e.operatorOrBot.id})")
        is MemberLeaveEvent.Quit -> eventLog(level, "[U:${e.member.nameCardOrNick}(${e.member.id})][G:${e.group.name}(${e.group.id})] -> <LEAVE> quit")
        else -> eventLog(level, "[U:${e.member.nameCardOrNick}(${e.member.id})][G:${e.group.name}(${e.group.id})] -> <LEAVE> ${e::class.simpleName}")
    }

    /*  ========================  CLI  ========================  */

    fun cliLog(level: Level, msg: String) = cliLogger.log(level, msg)

    fun log(cmd: CmdExecutable, c: CmdContext, info: String, level: Level = Level.INFO) =
        cliLog(level, "[U:${c.user.name}(${c.user.id})][G:${c.group.name}(${c.group.id})] <${cmd.id}> $info")

    fun log(cmd: CmdExecutable, info: String, level: Level = Level.INFO) =
        cliLog(level, "<${cmd.id}> $info")

    fun cliError(e: Throwable, level: Level) =
        cliLog(level, e::class.simpleName + ": " + e.message + "\n" + e.stackTraceToString())
}