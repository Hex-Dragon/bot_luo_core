package bot_luo_core.bot

import bot_luo_core.data.Group
import bot_luo_core.data.User
import bot_luo_core.util.Logger
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toMessageChain
import net.mamoe.mirai.message.data.toPlainText

interface BotContact {
    val id: Long
    var bot: Bot?
    val contactType: BotContactType
    val contact: Contact?

    suspend fun sendMessage(msg: Message): MessageReceipt<Contact>? = contact?.sendMessage(msg)
    suspend fun sendMessage(str: String): MessageReceipt<Contact>? = contact?.sendMessage(str)
    suspend fun sendMessageWithLog(msg: Message): MessageReceipt<Contact>? {
        when (contactType) {
            BotContactType.Single -> Logger.msgLog(msg.toMessageChain(), this as User, null)
            BotContactType.Group -> Logger.msgLog(msg.toMessageChain(), null, this as Group)
        }
        return sendMessage(msg)
    }
    suspend fun sendMessageWithLog(str: String): MessageReceipt<Contact>? = sendMessageWithLog(str.toPlainText().toMessageChain())
}