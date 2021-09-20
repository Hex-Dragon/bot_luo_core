package bot_luo_core.data

import bot_luo_core.bot.BotContact
import bot_luo_core.bot.BotContactType
import bot_luo_core.bot.BotLuo
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission

/**
 * # 群组类
 *
 * 使用[Groups.readGroup]或[Groups.readGroupOrNull]方法获取实例
 */
class Group internal constructor(
    override val id: Long,
    override var bot: Bot? = null
) : CmdDataObj("data/groups/$id.json", 30000L, true), BotContact {
    override val contactType = BotContactType.Group
    override val defaultData: CmdData
        get() = CmdData(0,0,0,false)//群组命令默认关闭
    override var contact: Contact? = null
        get() {
            if (field == null)
                field = BotLuo.getMiraiContact(this)
            return field
        }

    val virtual: Boolean = id==0L

    override fun unload() {
        Groups.remove(this)
    }

    val name: String
        get() = (contact as Group?)?.name ?: getObj("name") ?: ""

    var pmsGroup: PmsGroup
        get() = PmsGroups.getPmsGroup(getObj("pmsGroup")?: "NONE")
        set(value) = setObj("pmsGroup", value.name)

    var mutedExceptions: ArrayList<String>
        get() = getObj("mutedExceptions")?: Config.MUTED_EXCEPTIONS
        set(value) = setObj("mutedExceptions", value)

    var mutedCheckers: ArrayList<String>
        get() = getObj("mutedCheckers")?: Config.MUTED_CHECKERS
        set(value) = setObj("mutedCheckers", value)

    inline fun withServeBot(pms: MemberPermission = MemberPermission.MEMBER, action: (contact: Group)->Unit): Boolean {
        if (virtual) return false
        val contact = if (bot == null) BotLuo.getContactableBots(this).map { it.getGroup(id) }.filter { it != null && it.botPermission >= pms }.randomOrNull()
        else bot?.getGroup(id)
        return if (contact == null) false
        else {
            action(contact)
            true
        }
    }
}