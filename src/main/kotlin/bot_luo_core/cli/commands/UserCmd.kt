package bot_luo_core.cli.commands

import bot_luo_core.bot.BotLuo
import bot_luo_core.cli.Cmd
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdPermissionLevel
import bot_luo_core.cli.CmdReceipt
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.handlers.MultiGroupArgHandler
import bot_luo_core.cli.handlers.UserArgHandler
import bot_luo_core.data.Group
import bot_luo_core.data.GroupTags
import bot_luo_core.data.User
import bot_luo_core.util.TableBuilder

@Command(
    name = "user",
    display = "用户",
    alias = [],
    usage = "用户相关操作命令"
)
class UserCmd(context: CmdContext) : Cmd(context) {

    @Method(name = "in", alias = [], pmsLevel = CmdPermissionLevel.OP, title = "所在群组")
    fun userIn(
        @Argument(name = "用户", handler = UserArgHandler::class)
        user: User,
        @Argument(name = "限定群组", multiValued = true, required = false, handler = MultiGroupArgHandler::class)
        groupsIn: ArrayList<Group>?
    ): CmdReceipt {
        val groups = groupsIn ?: GroupTags.readGroupTag("#all") ?: arrayListOf(context.group)
        val table = TableBuilder(4)
        table.th("所在群组 —— ${user.name}(${user.id})").br()
        for (g in groups) {
            val group = (BotLuo.getMiraiContact(g) ?: continue) as net.mamoe.mirai.contact.Group
            if (group.contains(user.id))
                table.tr("${group.name}(${group.id})")
        }
        context.print(table.toString())
        return SUCCESS
    }
}