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
    usage = "查看或设置权限",
    caption = [
        "使用pg命令查看或编辑权限组"
    ]
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
        table.th("权限 —— 群组 ${group.name}(${group.id})").br()
        table.tr("权限组：").td(group.pmsGroup.name)
        table.tr("描述：").td(group.pmsGroup.description)
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
        table.th("权限 —— 用户 ${user.name}(${user.id})").br()
        table.tr("权限组：").td(user.pmsGroup.name)
        table.tr("描述：").td(user.pmsGroup.description)
        context.print(table.toString())
        return SUCCESS
    }

    /*  ========================  set  ========================  */

    @Method(name = "set-group",alias = ["sg"],pmsLevel = CmdPermissionLevel.OP,title = "设置群组", order = 0)
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
            table.tr("${group.name}(${group.id})").td(group.pmsGroup.name).td("->").td(pms.name)
            group.pmsGroup = pms
        } }
        context.print(table.toString())
        return SUCCESS
    }
    @Method(name = "set-group",alias = ["sg"],pmsLevel = CmdPermissionLevel.OP,title = "设置群组", order = 1)
    suspend fun setG(
        @Argument(name = "群组", required = false, multiValued = true, handler = MultiGroupArgHandler::class)
        groupsIn: ArrayList<Group>?,
        @Argument(name = "权限组", handler = PmsGroupArgHandler::class)
        pms: PmsGroup
    ): CmdReceipt = setG(pms, groupsIn)

    @Method(name = "set-user",alias = ["su"],pmsLevel = CmdPermissionLevel.OP,title = "设置用户",order = 0)
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
            table.tr("${user.name}(${user.id})").td(user.pmsGroup.name).td("->").td(pms.name)
            user.pmsGroup = pms
        } }
        context.print(table.toString())
        return SUCCESS
    }
    @Method(name = "set-user",alias = ["su"],pmsLevel = CmdPermissionLevel.OP,title = "设置用户",order = 1)
    suspend fun setU(
        @Argument(name = "用户", required = false, multiValued = true, handler = MultiUserArgHandler::class)
        usersIn: ArrayList<User>?,
        @Argument(name = "权限组", handler = PmsGroupArgHandler::class)
        pms: PmsGroup
    ): CmdReceipt = setU(pms, usersIn)
}