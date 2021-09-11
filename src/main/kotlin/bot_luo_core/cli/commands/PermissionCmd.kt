package bot_luo_core.cli.commands

import bot_luo_core.cli.Cmd
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdReceipt
import bot_luo_core.cli.CmdPermissionLevel
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.handlers.*
import bot_luo_core.data.Group
import bot_luo_core.data.PmsGroup
import bot_luo_core.data.User
import bot_luo_core.data.withLockedAccessing
import bot_luo_core.util.TableBuilder

@Command(
    name = "permission",
    display = "权限",
    alias = ["pms"],
    usage = "查看或设置权限"
)
class PermissionCmd(context: CmdContext) : Cmd(context) {

    /*  ========================  get  ========================  */

    @Method(name = "",alias = ["get-group","gg","g"],pmsLevel = CmdPermissionLevel.OP,title = "查看群组")
    fun getG(
        @Argument(name = "群组", required = false, handler = GroupArgHandler::class)
        groupIn: Group?
    ): CmdReceipt {
        val group = groupIn?: context.group
        val table = TableBuilder(4)
        table.th("权限组 —— 群组 ${group.name}(${group.id})").br()
        table.tr(group.pmsGroup.name)
        context.print(table.toString())
        return SUCCESS
    }

    @Method(name = "get-user",alias = ["gu","u"],pmsLevel = CmdPermissionLevel.OP,title = "查看用户")
    fun getU(
        @Argument(name = "用户", required = false, handler = UserArgHandler::class)
        userIn: User?
    ): CmdReceipt {
        val user = userIn?: context.user
        val table = TableBuilder(4)
        table.th("权限组 —— 用户 ${user.name}(${user.id})").br()
        table.tr(user.pmsGroup.name)
        context.print(table.toString())
        return SUCCESS
    }

    /*  ========================  set  ========================  */

    @Method(name = "set-group",alias = ["sg"],pmsLevel = CmdPermissionLevel.OP,title = "设置群组")
    suspend fun setG(
        @Argument(name = "权限组", handler = PmsGroupArgHandler::class)
        pms: PmsGroup,
        @Argument(name = "群组", required = false, multiValued = true, handler = MultiGroupArgHandler::class)
        groupsIn: ArrayList<Group>?
    ): CmdReceipt {
        val groups = groupsIn?: arrayListOf(context.group)
        val table = TableBuilder(4)
        table.th("更改权限组 —— 群组").br()
        for (group in groups) { withLockedAccessing(group) {
            table.tr("${group.name}(${group.id})").tb(group.pmsGroup.name).tb("->").tb(pms.name)
            group.pmsGroup = pms
        } }
        context.print(table.toString())
        return SUCCESS
    }

    @Method(name = "set-user",alias = ["su"],pmsLevel = CmdPermissionLevel.OP,title = "设置用户")
    suspend fun setU(
        @Argument(name = "权限组", handler = PmsGroupArgHandler::class)
        pms: PmsGroup,
        @Argument(name = "用户", required = false, multiValued = true, handler = MultiUserArgHandler::class)
        usersIn: ArrayList<User>?
    ): CmdReceipt {
        val users = usersIn?: arrayListOf(context.user)
        val table = TableBuilder(4)
        table.th("更改权限组 —— 用户").br()
        for (user in users) { withLockedAccessing(user) {
            table.tr("${user.name}(${user.id})").tb(user.pmsGroup.name).tb("->").tb(pms.name)
            user.pmsGroup = pms
        } }
        context.print(table.toString())
        return SUCCESS
    }
}