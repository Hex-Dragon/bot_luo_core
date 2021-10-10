package bot_luo_core.cli.commands

import bot_luo_core.cli.Cmd
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdPermissionLevel
import bot_luo_core.cli.CmdReceipt
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.handlers.GreedyMessageArgHandler
import bot_luo_core.cli.handlers.MultiGroupArgHandler
import bot_luo_core.cli.handlers.UserArgHandler
import bot_luo_core.data.*
import bot_luo_core.util.ResourceManager.downloadResource
import bot_luo_core.util.TableBuilder
import bot_luo_core.util.Text.limitMid
import bot_luo_core.util.Text.miraiCodeContent
import com.github.salomonbrys.kotson.keys
import net.mamoe.mirai.message.data.MessageChain

@Command(
    name = "notice",
    display = "提醒",
    alias = [],
    usage = "对用户添加提醒，当指定用户下一次发言时发送提醒消息",
    caption = [
        "不设置限定群组时默认为当前群组"
    ]
)
class NoticeCmd(context: CmdContext) : Cmd(context) {

    /*  ========================  add  ========================  */

    @Method(name = "add-ex", alias = ["", "add", "a"], pmsLevel = CmdPermissionLevel.OP, order = 0)
    suspend fun add(
        @Argument(name = "用户", handler = UserArgHandler::class)
        user: User,
        @Argument(name = "限定群组", multiValued = true, handler = MultiGroupArgHandler::class)
        groups: ArrayList<Group>,
        @Argument(name = "消息", handler = GreedyMessageArgHandler::class)
        msg: MessageChain
    ): CmdReceipt {
        withLockedAccessing(Notice) {
            msg.downloadResource()
            val list = Notice[user.id] ?: ArrayList()
            list.add(
                NoticeData(
                    context.user.id,
                    context.user.name,
                    msg.serializeToMiraiCode(),
                    context.time,
                    groups.map { it.id }.toSet()
                )
            )
            Notice[user.id] = list
        }
        context.sendMessageWithLog(context.atOrEmpty + "已添加")
        return SUCCESS
    }

    @Method(name = "", alias = ["add", "a"], pmsLevel = CmdPermissionLevel.HIGH, order = 1)
    suspend fun add(
        @Argument(name = "用户", handler = UserArgHandler::class)
        user: User,
        @Argument(name = "消息", handler = GreedyMessageArgHandler::class)
        msg: MessageChain
    ): CmdReceipt = add(user, arrayListOf(context.group), msg)

    /*  ========================  list  ========================  */

    @Method(name = "list", alias = ["l"], pmsLevel = CmdPermissionLevel.OP)
    suspend fun list(): CmdReceipt {
        val table = TableBuilder(4)
        table.th("提醒列表 ——").br()
        withLockedAccessing(Notice) {
            for (id in Notice.element.keys()) {
                table.tr(id).td("${Notice[id]?.size}个提醒")
            }
        }
        context.print(table.toString())
        return SUCCESS
    }

    @Method(name = "list", alias = ["l"], pmsLevel = CmdPermissionLevel.OP)
    suspend fun list(
        @Argument(name = "用户", handler = UserArgHandler::class)
        user: User
    ): CmdReceipt {
        val table = TableBuilder(4)
        table.th("提醒列表 —— ${user.name}(${user.id})").br()
        withLockedAccessing(Notice) {
            val list = Notice[user.id] ?: ArrayList()
            for (n in list) {
                table.tr(n.fromName.limitMid(20) + "(${n.fromId})").td(n.message.miraiCodeContent.limitMid(40))
            }
        }
        context.print(table.toString())
        return SUCCESS
    }

}