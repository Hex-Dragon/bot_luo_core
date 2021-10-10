package bot_luo_core.cli.commands

import bot_luo_core.cli.*
import bot_luo_core.cli.CmdCatalog.COMMANDS
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.checkers.PermissionChecker
import bot_luo_core.cli.checkers.addon.GroupPermissionChecker
import bot_luo_core.cli.exceptions.CheckerFatal
import bot_luo_core.cli.handlers.CmdExArgHandler
import bot_luo_core.data.CmdDataObj
import bot_luo_core.data.Cmds
import bot_luo_core.util.TableBuilder
import bot_luo_core.util.Time.relativeToE
import kotlin.reflect.full.createInstance

@Command(
    name = "stat",
    display = "统计",
    alias = [],
    usage = "查看罗伯特业绩"
)
class StatCmd(context: CmdContext) : Cmd(context) {

    fun getDetail(carrier: CmdDataObj, name: String, id: String, cmds: List<CmdExecutable>): CmdReceipt {
        val filtered = cmds.filter {
            try {
                justCheck.forEach { checker -> checker.createInstance().check(it, context) }
                true
            } catch (ignore: CheckerFatal) {
                false
            }
        }

        return if (filtered.isEmpty()) {
            context.print("${name}统计 —— 403 Forbidden")
            FATAL
        } else {
            val table = TableBuilder(4)
            filtered.groupBy { it.cmdName }.forEach { (k, v) ->
                table.th("${name}统计 —— $id").br()
                val map = v.associateBy { it.methName }
                table.prettyLines(
                    "${k}：${map.values.sumOf { carrier.readCmdData(it).totalCount }}次",
                    map.entries
                ) { (key, value), builder ->
                        builder.td("-$key:")
                        .td("${carrier.readCmdData(value).totalCount}次")
                        .td(carrier.readCmdData(value).lastTime relativeToE context.time)
                }
            }
            context.print(table.toString())
            SUCCESS
        }
    }

    fun getGeneral(carrier: CmdDataObj, name: String, id: String, cmds: ArrayList<CmdExecutable>): CmdReceipt {
        val filtered = cmds.filter {
            try {
                justCheck.forEach { checker -> checker.createInstance().check(it, context) }
                true
            } catch (ignore: CheckerFatal) {
                false
            }
        }

        return if (filtered.isEmpty()) {
            context.print("${name}统计 —— 403 Forbidden")
            FATAL
        } else {
            val table = TableBuilder(4)
            table.th("${name}统计 —— $id").br()
            filtered.groupBy { it.cmdName }.forEach { (k, v) ->
                val map = v.associateBy { it.methName }
                val total = map.values.sumOf { carrier.readCmdData(it).totalCount }
                if (total > 0)
                table.tr("$k:")
                    .td("${total}次")
                    .td(map.values.maxOf { carrier.readCmdData(it).lastTime } relativeToE context.time)
            }
            context.print(table.toString())
            SUCCESS
        }
    }

    /*  ========================  cmd  ========================  */

    @Method(name = "", alias = ["cmd", "c"], pmsLevel = CmdPermissionLevel.HIGH, title = "全局统计", order = 0)
    fun cmd (
        @Argument(name = "命令", handler = CmdExArgHandler::class)
        cmds: List<CmdExecutable>
    ): CmdReceipt = getDetail(Cmds, "全局命令", "", cmds)

    @Method(name = "", alias = ["cmd", "c"], pmsLevel = CmdPermissionLevel.HIGH, title = "全局统计", order = 1)
    fun cmd (): CmdReceipt = getGeneral(Cmds, "全局命令", "", COMMANDS)

    /*  ========================  group  ========================  */

    @Method(name = "group", alias = ["g"], pmsLevel = CmdPermissionLevel.HIGH, title = "群组统计", order = 0)
    fun group (
        @Argument(name = "命令", handler = CmdExArgHandler::class)
        cmds: List<CmdExecutable>
    ): CmdReceipt = getDetail(context.group, "群组", "${context.group.name}(${context.group.id})", cmds)

    @Method(name = "group", alias = ["g"], pmsLevel = CmdPermissionLevel.HIGH, title = "群组统计", order = 1)
    fun group (): CmdReceipt = getGeneral(context.group, "群组", "${context.group.name}(${context.group.id})", COMMANDS)

    /*  ========================  user  ========================  */

    @Method(name = "user", alias = ["u"], pmsLevel = CmdPermissionLevel.HIGH, title = "用户统计", order = 0)
    fun user (
        @Argument(name = "命令", handler = CmdExArgHandler::class)
        cmds: List<CmdExecutable>
    ): CmdReceipt = getDetail(context.user, "用户", "${context.user.name}(${context.user.id})", cmds)

    @Method(name = "user", alias = ["u"], pmsLevel = CmdPermissionLevel.HIGH, title = "用户统计", order = 1)
    fun user (): CmdReceipt = getGeneral(context.user, "用户", "${context.user.name}(${context.user.id})", COMMANDS)

    companion object {

        val justCheck = arrayOf(
            GroupPermissionChecker::class
        )
    }
}