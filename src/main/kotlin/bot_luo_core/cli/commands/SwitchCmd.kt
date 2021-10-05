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
import bot_luo_core.data.Group
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

    @Method(name = "", alias = ["show"], pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class],
    title = "查看", usage = "获取命令是否开启")
    fun show(
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>,
        @Argument(name = "群组", handler = GroupArgHandler::class, required = false)
        groupIn: Group?
    ): CmdReceipt {
        val group = groupIn?: context.group
        val table = TableBuilder(4)
        table.th("电闸 —— 群组(${group.id})").br()
        for (id in ids) {
            table.tr(id).td(":").td(if (group.getCmdWorking(id)) "ON" else "OFF")
        }
        context.println(table.toString())
        return SUCCESS
    }

    @Method(name = "", alias = ["show"], pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class])
    fun show(
        @Argument(name = "群组", handler = GroupArgHandler::class)
        groupIn: Group,
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>
    ) = show(ids, groupIn)

    /*  ========================  on  ========================  */

    @Method(name = "on", pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class])
    suspend fun on(
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>,
        @Argument(name = "群组", handler = GroupArgHandler::class, required = false)
        groupIn: Group?
    ): CmdReceipt {
        val group = groupIn?: context.group
        val table = TableBuilder(4)
        table.th("电闸开启 —— 群组(${group.id})").br()
        withLockedAccessing(group) {
            for (id in ids) {
                table.tr(id).td(":").td(if (group.getCmdWorking(id)) "ON" else "OFF").td("->").td("ON")
                group.setCmdWorking(id, true)
            }
        }
        context.println(table.toString())
        return SUCCESS
    }

    @Method(name = "on", pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class])
    suspend fun on(
        @Argument(name = "群组", handler = GroupArgHandler::class)
        groupIn: Group,
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>
    ) = on (ids, groupIn)

    /*  ========================  off  ========================  */

    @Method(name = "off", pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class])
    suspend fun off(
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>,
        @Argument(name = "群组", handler = GroupArgHandler::class, required = false)
        groupIn: Group?
    ): CmdReceipt {
        val group = groupIn?: context.group
        val table = TableBuilder(4)
        table.th("电闸关闭 —— 群组(${group.id})").br()
        withLockedAccessing(group) {
            for (id in ids) {
                table.tr(id).td(":").td(if (group.getCmdWorking(id)) "ON" else "OFF").td("->").td("OFF")
                group.setCmdWorking(id, false)
            }
        }
        context.println(table.toString())
        return SUCCESS
    }

    @Method(name = "off", pmsLevel = CmdPermissionLevel.OP, order = 0, ignoreCheckers = [GroupCmdWorkingChecker::class])
    suspend fun off(
        @Argument(name = "群组", handler = GroupArgHandler::class)
        groupIn: Group,
        @Argument(name = "命令", handler = CmdIdArgHandler::class, multiValued = true)
        ids: ArrayList<String>
    ) = off (ids, groupIn)
}