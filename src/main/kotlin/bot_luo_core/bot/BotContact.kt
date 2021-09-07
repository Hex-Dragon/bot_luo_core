package bot_luo_core.bot

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message

interface BotContact {
    val id: Long
    var bot: Bot?
    val contactType: BotContactType
    val contact: Contact? get() = MultiBotHandler.getMiraiContact(this)

    suspend fun sendMessage(msg: Message): MessageReceipt<Contact>? = contact?.sendMessage(msg)
    suspend fun sendMessage(str: String): MessageReceipt<Contact>? = contact?.sendMessage(str)
}