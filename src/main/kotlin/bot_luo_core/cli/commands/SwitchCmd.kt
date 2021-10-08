package bot_luo_core.cli.commands

import bot_luo_core.cli.Cmd
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdReceipt
import bot_luo_core.cli.CmdPermissionLevel
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.checkers.GroupCmdWorkingChecker
import bot_luo_core.cli.handlers.CmdIdArgHandler
import bot_luo_core.cli.handlers.GroupArgHandler
import bot_luo_core.cli.handlers.UserArgHandler
import bot_luo_core.data.Group
import bot_luo_core.data.User
import bot_luo_core.data.withLockedAccessing
import bot_luo_core.util.TableBuilder

@Command(
    name = "switch",
    display = "开关",
    alias = ["sw"],
    usage = "开启或关闭命令"
)
class SwitchCmd(context: CmdContext) : Cmd(context) {

    /*  ========================  show  ========================  */

    @Method(name = "", alias = ["group","g"], pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "查看群组", usage = "获取命令是否在群组开启")
    fun show(
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>,
        @Argument(name = "群组", handler = GroupArgHandler::class, required = false)
        groupIn: Group?
    ): CmdReceipt {
        val group = groupIn?: context.group
        val table = TableBuilder(4)
        table.th("群组电闸 —— ${group.name}(${group.id})").br()
        for (id in ids) {
            table.tr(id).td(":").td(if (group.getCmdWorking(id)) "ON" else "OFF")
        }
        context.println(table.toString())
        return SUCCESS
    }

    @Method(name = "user", alias = ["u"], pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "查看用户", usage = "获取命令是否对用户开启")
    fun show(
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>,
        @Argument(name = "用户", handler = UserArgHandler::class, required = false)
        userIn: User?
    ): CmdReceipt {
        val user = userIn?: context.user
        val table = TableBuilder(4)
        table.th("用户电闸 —— ${user.name}(${user.id})").br()
        for (id in ids) {
            table.tr(id).td(":").td(if (user.getCmdWorking(id)) "ON" else "OFF")
        }
        context.println(table.toString())
        return SUCCESS
    }

    @Method(name = "", alias = ["group","g"], pmsLevel = CmdPermissionLevel.OP, order = 1, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "查看群组", usage = "获取命令是否在群组开启")
    fun show(
        @Argument(name = "群组", handler = GroupArgHandler::class)
        groupIn: Group,
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>
    ) = show(ids, groupIn)

    @Method(name = "user", alias = ["u"], pmsLevel = CmdPermissionLevel.OP, order = 1, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "查看用户", usage = "获取命令是否对用户开启")
    fun show(
        @Argument(name = "用户", handler = UserArgHandler::class)
        userIn: User,
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>
    ) = show(ids, userIn)

    /*  ========================  on  ========================  */

    @Method(name = "group-on", alias = ["gon","on"], pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "群组开启")
    suspend fun on(
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>,
        @Argument(name = "群组", handler = GroupArgHandler::class, required = false)
        groupIn: Group?
    ): CmdReceipt {
        val group = groupIn?: context.group
        val table = TableBuilder(4)
        table.th("群组电闸开启 —— ${group.name}(${group.id})").br()
        withLockedAccessing(group) {
            for (id in ids) {
                table.tr(id).td(":").td(if (group.getCmdWorking(id)) "ON" else "OFF").td("->").td("ON")
                group.setCmdWorking(id, true)
            }
        }
        context.println(table.toString())
        return SUCCESS
    }

    @Method(name = "user-on", alias = ["uon"], pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "用户开启")
    suspend fun on(
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>,
        @Argument(name = "用户", handler = UserArgHandler::class, required = false)
        userIn: User?
    ): CmdReceipt {
        val user = userIn?: context.user
        val table = TableBuilder(4)
        table.th("用户电闸开启 —— ${user.name}(${user.id})").br()
        withLockedAccessing(user) {
            for (id in ids) {
                table.tr(id).td(":").td(if (user.getCmdWorking(id)) "ON" else "OFF").td("->").td("ON")
                user.setCmdWorking(id, true)
            }
        }
        context.println(table.toString())
        return SUCCESS
    }

    @Method(name = "group-on", alias = ["gon","on"], pmsLevel = CmdPermissionLevel.OP, order = 1, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "群组开启")
    suspend fun on(
        @Argument(name = "群组", handler = GroupArgHandler::class)
        groupIn: Group,
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>
    ) = on (ids, groupIn)

    @Method(name = "user-on", alias = ["uon"], pmsLevel = CmdPermissionLevel.OP, order = 1, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "用户开启")
    suspend fun on(
        @Argument(name = "用户", handler = UserArgHandler::class)
        userIn: User,
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>
    ) = on (ids, userIn)

    /*  ========================  off  ========================  */

    @Method(name = "group-off", alias = ["goff","off"], pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "群组关闭")
    suspend fun off(
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>,
        @Argument(name = "群组", handler = GroupArgHandler::class, required = false)
        groupIn: Group?
    ): CmdReceipt {
        val group = groupIn?: context.group
        val table = TableBuilder(4)
        table.th("群组电闸关闭 —— ${group.name}(${group.id})").br()
        withLockedAccessing(group) {
            for (id in ids) {
                table.tr(id).td(":").td(if (group.getCmdWorking(id)) "ON" else "OFF").td("->").td("OFF")
                group.setCmdWorking(id, false)
            }
        }
        context.println(table.toString())
        return SUCCESS
    }

    @Method(name = "user-off", alias = ["uoff"], pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "用户关闭")
    suspend fun off(
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>,
        @Argument(name = "用户", handler = UserArgHandler::class, required = false)
        userIn: User?
    ): CmdReceipt {
        val user = userIn?: context.user
        val table = TableBuilder(4)
        table.th("用户电闸关闭 —— ${user.name}(${user.id})").br()
        withLockedAccessing(user) {
            for (id in ids) {
                table.tr(id).td(":").td(if (user.getCmdWorking(id)) "ON" else "OFF").td("->").td("OFF")
                user.setCmdWorking(id, false)
            }
        }
        context.println(table.toString())
        return SUCCESS
    }

    @Method(name = "group-off", alias = ["goff","off"], pmsLevel = CmdPermissionLevel.OP, order = 1, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "群组关闭")
    suspend fun off(
        @Argument(name = "群组", handler = GroupArgHandler::class)
        groupIn: Group,
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>
    ) = off (ids, groupIn)

    @Method(name = "user-off", alias = ["uoff"], pmsLevel = CmdPermissionLevel.OP, order = 1, ignoreCheckers = [GroupCmdWorkingChecker::class],
        title = "用户关闭")
    suspend fun off(
        @Argument(name = "用户", handler = UserArgHandler::class)
        userIn: User,
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>
    ) = off (ids, userIn)
}