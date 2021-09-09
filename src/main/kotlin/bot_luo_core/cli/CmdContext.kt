package bot_luo_core.cli

import bot_luo_core.bot.BotContact
import bot_luo_core.bot.MultiBotHandler
import bot_luo_core.data.Config.REPL_NO
import bot_luo_core.data.Config.REPL_YES
import bot_luo_core.data.Group
import bot_luo_core.data.User
import bot_luo_core.util.Logger
import bot_luo_core.util.Logger.sendMessageWithLog
import bot_luo_core.util.Text.toLowercase
import bot_luo_core.util.Time
import kotlinx.coroutines.TimeoutCancellationException
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.RemoteFile.Companion.uploadFile
import java.io.File
import kotlin.jvm.Throws

class CmdContext(
    val reader: MessageReader,
    val user: User,
    val group: Group,
    val userF: User,
    val groupF: Group,
    val time: Long = Time.time()
) {

    constructor(
        reader: MessageReader,
        user: User,
        group: Group,
        time: Long = Time.time()
    ): this(reader, user, group, user, group, time)

    /**
     * - 0: 不允许上传
     * - 1: 当输出过大时允许上传文件
     * - 2: 始终上传
     */
    var uploadOutputFile: Int = 0

    /**
     * 直接回复消息所使用的对象，可能为`null`
     *
     * 使用[sendMessageWithLog]发送消息
     */
    val subject: BotContact?
        get() = when {
            !group.virtual -> group
            !user.virtual -> user
            else -> null
        }

    /**
     * 接受返回值消息的对象，可能为`null`
     *
     * 使用[sendOutputWithLog]发送消息
     */
    val receiver: BotContact?
        get() = when {
            !groupF.virtual -> groupF
            !userF.virtual -> userF
            else -> null
        }

    val atOrEmpty: Message get() {
        return if (!group.virtual && !user.virtual) user.getAt()
        else EmptyMessageChain
    }

    private val mcb = MessageChainBuilder()
    private val msgReceipt = ArrayList<Int>()

    fun print(str: String) = mcb.add(str)
    fun print(msg: Message) = mcb.add(msg)

    fun println(str: String) {
        print(str)
        print("\n")
    }
    fun println(msg: Message) {
        print(msg)
        print("\n")
    }
    fun println() {
        print("\n")
    }

    fun getOutput(): MessageChain = mcb.build()

    suspend inline fun sendMessageWithLog(str: String): MessageReceipt<Contact>? = sendMessageWithLog(str.toPlainText())
    suspend inline fun sendMessageWithLog(msg: Message): MessageReceipt<Contact>? = sendMessageWithLog(msg.toMessageChain())
    suspend fun sendMessageWithLog(msg: MessageChain): MessageReceipt<Contact>? {
        Logger.msgLog(msg, user, group)
        return try {
            subject?.sendMessage(msg)
        } catch (e: EventCancelledException) {
            null
        }
    }

    suspend inline fun sendOutputWithLog(str: String): MessageReceipt<Contact>? = sendOutputWithLog(str.toPlainText())
    suspend inline fun sendOutputWithLog(msg: Message): MessageReceipt<Contact>? = sendOutputWithLog(msg.toMessageChain())
    suspend fun sendOutputWithLog(msg: MessageChain): MessageReceipt<Contact>? {
        Logger.msgLog(msg, userF, groupF)
        return try {
            receiver?.sendMessage(msg)
        } catch (e: EventCancelledException) {
            null
        }
    }
    suspend fun sendOutputWithLog(): MessageReceipt<Contact>? {
        val msg = mcb.build()
        if (!msg.isContentEmpty()) {
            Logger.msgLog(msg, userF, groupF)
            return try {
                receiver?.sendMessage(msg)
            } catch (e: EventCancelledException) {
                null
            }
        }
        return null
    }

    suspend fun uploadOutput(): MessageReceipt<Contact>? {
        withBackendBot{ contact ->
            if (contact !is FileSupported) return null
            val file = File.createTempFile("output-${this.hashCode()}",".txt")
            file.deleteOnExit()
            file.toExternalResource().use { resource ->
                return  contact.sendMessageWithLog(contact.uploadFile("${this.reader.original.content.replace(' ','_')}.txt",resource))
            }
        }
        return null
    }

    @Throws(TimeoutCancellationException::class)
    suspend inline fun nextMessage(timeoutMillis: Long = -1L): MessageChain = MultiBotHandler.catchNextMessage(user.id, group.id, timeoutMillis)

    suspend inline fun nextMessage(timeoutMillis: Long = -1L, timeoutAction: () -> Unit): MessageChain {
        try {
            return MultiBotHandler.catchNextMessage(user.id, group.id, timeoutMillis)
        } catch (e: TimeoutCancellationException) {
            timeoutAction()
        }
        return EmptyMessageChain
    }
    suspend inline fun nextYorN(timeoutMillis: Long, noinline timeoutAction: suspend () -> Boolean): Boolean {
        return try {
            MultiBotHandler.catchNextMessage(user.id, group.id, timeoutMillis) {
                it.content.trim().toLowercase() in REPL_YES + REPL_NO
            }.content.trim().toLowercase() in REPL_YES
        } catch (e: TimeoutCancellationException) {
            timeoutAction()
        }
    }

    /**
     * 分支一个新的[CmdContext]，使用给的的环境
     */
    fun fork(
        reader: MessageReader = MessageReader(this.reader),
        user: User = this.user,
        group: Group = this.group,
        time: Long = this.time
    ): CmdContext = CmdContext(
        reader, user, group, userF, groupF, time
    )

    /**
     * 获取一个[subject]的Mirai联系对象进行动作
     *
     * 获取失败返回`false`并放弃执行动作
     *
     * @return 是否获取成功
     */
    inline fun withServeBot(pms: MemberPermission = MemberPermission.MEMBER, action: (contact: Contact)->Unit): Boolean {
        return when(subject) {
            is Group -> group.withServeBot(pms, action)
            is User -> user.withServeBot(action)
            else -> false
        }
    }

    /**
     * 获取一个[receiver]的Mirai联系对象进行动作
     *
     * 获取失败返回`false`并放弃执行动作
     *
     * @return 是否获取成功
     */
    inline fun withBackendBot(pms: MemberPermission = MemberPermission.MEMBER, action: (contact: Contact)->Unit): Boolean {
        return when(receiver) {
            is Group -> group.withServeBot(pms, action)
            is User -> user.withServeBot(action)
            else -> false
        }
    }

}
