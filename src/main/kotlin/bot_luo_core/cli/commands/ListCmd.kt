package bot_luo_core.cli.commands

import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.checkers.addon.GroupOriginalPermissionChecker
import bot_luo_core.cli.exceptions.CheckerFatal
import bot_luo_core.data.PmsGroups.readPmsOn
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
    fun list (): CmdReceipt { with(context) {
        val table = TableBuilder(4)
        table.th("命令列表 —— 群组 ${group.name}(${group.id})").br()

        val map = HashMap<String, Pair<Boolean, CmdExecutable>>()
        for (cmd in CmdCatalog.COMMANDS.distinctBy { it.id }.sortedBy { it.id }) {
            if (map[cmd.cmdName]?.first == true) continue
            try {
                justCheck.forEach { it.createInstance().check(cmd, context) }
                map[cmd.cmdName] = (group.realPmsGroup.readPmsOn(cmd) >= 0) to cmd
            } catch (ignore: CheckerFatal) {}
        }
        map.values.forEach { (working, cmd) ->
            table.tr(if (working) "[√]" else "[×]").td(cmd.display).td("(${cmd.cmdName})")
        }

        context.print(table.toString())

        return SUCCESS
    }}

    @Method(name = "detail", alias = ["d"], pmsLevel = CmdPermissionLevel.HIGH, title = "详细列表")
    fun listD (): CmdReceipt { with(context) {
        val table = TableBuilder(4)
        table.th("命令列表 —— 群组 ${group.name}(${group.id})").br()

        for (cex in CmdCatalog.COMMANDS.distinctBy { it.id }.sortedBy { it.id }) {
            try {
                justCheck.forEach { it.createInstance().check(cex, context) }
                table.tr(if (group.realPmsGroup.readPmsOn(cex) >= 0) "[√]" else "[×]").td(cex.idFixed).td(cex.subTitle)
            } catch (ignore: CheckerFatal) {}
        }

        context.print(table.toString())

        return SUCCESS
    }}

    @Method(name = "all", alias = ["a"], pmsLevel = CmdPermissionLevel.OP, title = "全部命令")
    fun listA (): CmdReceipt { with(context) {
        val table = TableBuilder(4)
        table.th("命令列表 —— 群组 ${group.name}(${group.id})").br()

        for (cex in CmdCatalog.COMMANDS.distinctBy { it.id }.sortedBy { it.id }) {
            table.tr(if (group.realPmsGroup.readPmsOn(cex) >= 0) "[√]" else "[×]").td(cex.idFixed).td(cex.subTitle)
        }

        context.print(table.toString())

        return SUCCESS
    }}

    companion object{
        val justCheck = arrayOf(
            GroupOriginalPermissionChecker::class
        )
    }
}