package bot_luo_core.data

import bot_luo_core.bot.BotLuo.isMainBotOf
import bot_luo_core.bot.VirtualMessageEvent
import bot_luo_core.util.Logger.sendMessageWithLog
import bot_luo_core.util.ResourceManager.deleteResourceIfNeeded
import bot_luo_core.util.ResourceManager.uploadResource
import bot_luo_core.util.Text.limitMid
import bot_luo_core.util.Time
import bot_luo_core.util.Time.relativeToE
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupAwareMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder

object Notice: DataObject("data/notice.json", 10000, false)
{
    init {
        GlobalEventChannel.subscribeAlways<MessageEvent>(priority = EventPriority.HIGH) {
            if (this is GroupMessageEvent && !bot.isMainBotOf(this.group.id)) return@subscribeAlways
            check(
                Users.readUser(this.sender.id),
                if (this is GroupAwareMessageEvent) Groups.readGroup(this.group.id) else Groups.virtualGroup,
                this.bot
            )
        }
        GlobalEventChannel.subscribeAlways<VirtualMessageEvent>(priority = EventPriority.HIGH) {
            check(
                user,
                group,
                null
            )
        }
    }

    operator fun get(id: String): ArrayList<NoticeData>? {
        return getObj<ArrayList<NoticeData>>(id)
    }
    operator fun get(id: Long): ArrayList<NoticeData>? {
        return getObj<ArrayList<NoticeData>>(id.toString())
    }

    operator fun set(id: Long, value: ArrayList<NoticeData>) {
        setObj(id.toString(), value)
    }

    suspend fun check(user: User, group: Group, botIn: Bot?) {
        withLockedAccessing(this) {
            val list = get(user.id)?.filter { group.id in it.groups }?.ifEmpty { null } ?: return

            suspend fun send(contact: Contact) {
                val mcb = MessageChainBuilder()
                for (i in list.indices) {
                    val n = list[i]
                    mcb.add(At(user.id))
                    mcb.add("${n.fromName.limitMid(10)}(${n.fromId})于${n.time relativeToE Time.time()}提醒你：\n")
                    val raw = n.message.deserializeMiraiCode()
                    val msg = raw.uploadResource(contact)
                    mcb.add(msg)
                    if (i < list.lastIndex) mcb.add("\n\n")
                    raw.deleteResourceIfNeeded()
                }
                contact.sendMessageWithLog(mcb.build())
            }

            if (!group.virtual)
                group.withServeBot {
                    send(it)
                }
            else
                user.apply { bot = botIn }.withServeBot {
                    send(it)
                }
            removeObj(user.id.toString())
        }
    }
}

data class NoticeData(
    val fromId: Long,
    val fromName: String,
    val message: String,
    val time: Long,
    val groups: Set<Long>
)