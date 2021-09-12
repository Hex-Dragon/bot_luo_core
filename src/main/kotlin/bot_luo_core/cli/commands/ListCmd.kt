package bot_luo_core.cli.commands

import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.checkers.addon.GroupPermissionChecker
import bot_luo_core.cli.exceptions.CheckerFatal
import bot_luo_core.util.TableBuilder
import kotlin.reflect.full.createInstance

@Command(
    name = "list",
    display = "命令列表",
    alias = [],
    usage = "显示可用命令"
)
class ListCmd(context: CmdContext) : Cmd(context) {

    @Method(name = "", alias = [], pmsLevel = CmdPermissionLevel.NORMAL, title = "命令列表")
    suspend fun list (): CmdReceipt { with(context) {
        val table = TableBuilder(4)
        table.th("命令列表 —— 群组 ${group.name}(${group.id})").br()

        val map = HashMap<String, Pair<Boolean, CmdExecutable>>()
        for (cmd in CmdCatalog.COMMANDS.distinctBy { it.id }.sortedBy { it.id }) {
            if (map[cmd.cmdName]?.first == true) continue
            try {
                justCheck.forEach { it.createInstance().check(cmd, context) }
                map[cmd.cmdName] = group.readCmdData(cmd).working to cmd
            } catch (ignore: CheckerFatal) {}
        }
        map.values.forEach { (working, cmd) ->
            table.tr(if (working) "[√]" else "[×]").tb(cmd.display).tb("(${cmd.cmdName})")
        }

        sendMessageWithLog(table.toString())

        return SUCCESS
    }}

    @Method(name = "detail", alias = ["d"], pmsLevel = CmdPermissionLevel.HIGH, title = "详细列表")
    suspend fun listD (): CmdReceipt { with(context) {
        val table = TableBuilder(4)
        table.th("命令列表 —— 群组 ${group.name}(${group.id})").br()

        for (cex in CmdCatalog.COMMANDS.distinctBy { it.id }.sortedBy { it.id }) {
            try {
                justCheck.forEach { it.createInstance().check(cex, context) }
                table.tr(if (group.readCmdData(cex).working) "[√]" else "[×]").tb(cex.idFixed).tb(cex.subTitle)
            } catch (ignore: CheckerFatal) {}
        }

        sendMessageWithLog(table.toString())

        return SUCCESS
    }}

    @Method(name = "all", alias = ["a"], pmsLevel = CmdPermissionLevel.OP, title = "全部命令")
    suspend fun listA (): CmdReceipt { with(context) {
        val table = TableBuilder(4)
        table.th("命令列表 —— 群组 ${group.name}(${group.id})").br()

        for (cex in CmdCatalog.COMMANDS.distinctBy { it.id }.sortedBy { it.id }) {
            table.tr(if (group.readCmdData(cex).working) "[√]" else "[×]").tb(cex.idFixed).tb(cex.subTitle)
        }

        sendMessageWithLog(table.toString())

        return SUCCESS
    }}

    companion object{
        val justCheck = arrayOf(
            GroupPermissionChecker::class
        )
    }
}