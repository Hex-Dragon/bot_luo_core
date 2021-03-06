package bot_luo_core.data

import bot_luo_core.bot.BotContact
import bot_luo_core.bot.BotContactType
import bot_luo_core.bot.BotLuo
import bot_luo_core.cli.CmdExecutable
import bot_luo_core.cli.CmdReceipt
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.At

/**
 * # 用户类
 *
 * 使用[Users.readUser]或[Users.readUserOrNull]方法获取实例
 */
class User internal constructor (
    override val id: Long,
    override var bot: Bot? = null
) : CmdDataObj("data/users/$id.json", 15000L, true), BotContact {
    override val contactType = BotContactType.Single
    override val defaultData: CmdData get() = CmdData(0,0,0,true)//用户命令默认开启
    override var contact: Contact? = null
        get() {
            if (field == null)
            field = BotLuo.getMiraiContact(this)
            return field
        }

    val virtual: Boolean = id==0L

    var cmdHistory: ArrayList<CmdReceipt>
        get() = getObj("cmdHistory")?: ArrayList()
        set(value) = setObj("cmdHistory", value)

    override fun unload() {
        Users.remove(this)
    }

    val name: String
        get() = when(contact) {
            is Member -> (contact as NormalMember).nameCardOrNick
            is User -> (contact as User).nameCardOrNick
            else -> ""
        }

    fun getAt() = At(id)

    var pmsGroup: PmsGroup
        get() = PmsGroups[(getObj("pmsGroup")?: "NONE")]
        set(value) = setObj("pmsGroup", value.name)

    fun getCmdWorking(id: String): Boolean {
        return readCmdData(id).working ?: true //用户默认开启
    }
    fun getCmdWorking(cmd: CmdExecutable): Boolean {
        return readCmdData(cmd).working ?: true //用户默认开启
    }

    fun setCmdWorking(id: String, value: Boolean) {
        return writeCmdData(id, readCmdData(id).apply { working = value })
    }

    fun setCmdWorking(cmd: CmdExecutable, value: Boolean) {
        return writeCmdData(cmd, readCmdData(cmd).apply { working = value })
    }

    inline fun withServeBot(action: (contact: User)->Unit): Boolean {
        if (virtual) return false
        val contact = if (bot == null) BotLuo.getContactableBots(this).map { it.getFriend(id)?:it.getStranger(id) }.randomOrNull()
        else bot?.getFriend(id)?:bot?.getStranger(id)
        return if (contact == null) false
        else {
            action(contact)
            true
        }
    }
}