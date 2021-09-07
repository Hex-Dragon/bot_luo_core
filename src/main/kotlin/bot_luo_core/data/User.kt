package bot_luo_core.data

import bot_luo_core.bot.BotContact
import bot_luo_core.bot.BotContactType
import bot_luo_core.bot.MultiBotHandler
import bot_luo_core.cli.CmdExecutable
import bot_luo_core.cli.CmdReceipt
import com.alibaba.fastjson.annotation.JSONField
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import kotlin.coroutines.CoroutineContext

/**
 * # 用户类
 *
 * 使用[Users.readUser]或[Users.readUserOrNull]方法获取实例
 */
class User internal constructor (
    override val id: Long,
    override var bot: Bot? = null
) : CmdDataObj("data/users/$id.json"), BotContact {
    override val contactType = BotContactType.Single
    override val defaultData: CmdData get() = CmdData(0,0,0,0,true)//用户命令默认开启

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
        get() = PmsGroups.getPmsGroup(getObj("pmsGroup")?: "NONE")
        set(value) = setObj("pmsGroup", value.name)

    inline fun withServeBot(action: (contact: User)->Unit): Boolean {
        if (virtual) return false
        val contact = if (bot == null) MultiBotHandler.getContactableBots(this).map { it.getFriend(id)?:it.getStranger(id) }.randomOrNull()
        else bot?.getFriend(id)?:bot?.getStranger(id)
        return if (contact == null) false
        else {
            action(contact)
            true
        }
    }
}