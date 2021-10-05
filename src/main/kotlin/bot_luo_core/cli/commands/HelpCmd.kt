package bot_luo_core.cli.commands

import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.checkers.PermissionChecker
import bot_luo_core.cli.checkers.addon.GroupPermissionChecker
import bot_luo_core.cli.exceptions.CheckerFatal
import bot_luo_core.cli.handlers.CmdExArgHandler
import bot_luo_core.util.TableBuilder
import kotlin.reflect.full.createInstance

/**
 * ## 使用自定义帮助 ##
 *
 * 自定义一个有相同[Command]注解的命令类并注册以定义帮助
 *
 * 例如：
 *
 * ```
 *
 * @Command(
 *  name = "help",
 *  display = "帮助",
 *  alias = ["?","？"]
 *  )
 *  class HelpCmd(context: CmdContext) : Cmd(context) {
 *
 *      @Method(name = "", alias = [], pmsLevel = CmdPermissionLevel.NORMAL, order = 0)
 *      suspend fun help(): CmdReceipt {
 *          context.sendMessage("这是您点的帮助")
 *          return SUCCESS
 *      }
 *  }
 *
 * ```
 */
@Command(
    name = "help",
    display = "帮助",
    alias = ["?","？"]
)
class HelpCmd(context: CmdContext) : Cmd(context) {

    /*  ========================  cmd  ========================  */

    @Method(name = "cmd", alias = ["c"], pmsLevel = CmdPermissionLevel.NORMAL)
    suspend fun helpCmd(
        @Argument(name = "命令", required = true, handler = CmdExArgHandler::class)
        cmds: List<CmdExecutable>
    ): CmdReceipt {

        val filtered = cmds.filter {
            try {
                justCheck.forEach{checker -> checker.createInstance().check(it,context)}
                true
            } catch (ignore: CheckerFatal) {
                false
            }
        }

        return if (filtered.isEmpty()) {
            context.sendMessageWithLog("帮助 —— 403 Forbidden")
            FATAL
        } else {
            context.sendMessageWithLog(cmdHelpGen(filtered)!!)
            SUCCESS
        }
    }

    companion object {

        val justCheck = arrayOf(
            GroupPermissionChecker::class
        )

        fun cmdHelpGen(cmds: List<CmdExecutable>): String? {
            if (cmds.isEmpty()) return null
            val table = TableBuilder(4)
            cmds.groupBy { it.cmdName }.forEach{ (k ,v ) ->
                val map = HashMap<String, ArrayList<CmdExecutable>>()
                val spl = ArrayList<String>()
                for (c in v) {
                    if (map[c.methName] == null) map[c.methName] = ArrayList()
                    map[c.methName]!!.add(c)
                    c.simples.forEach {
                        spl.add(c.head+"  "+it)
                    }
                }
                table.th("帮助 —— 命令($k)[${v[0].cmdHead.joinToString(",")}]").br()
                table.p(v[0].usage).br().br()
                map.forEach { (_ ,value ) ->
                    table.prettyLines(value[0].subTitle, value) { item, builder ->
                        builder.td(item.headPara).td(item.getParasFormat())
                    }.td(value[0].subUsage)
                }
                table.th().prettyLines("示例：",spl) { item, builder ->
                    builder.td(item)
                }
                table.th().prettyLines("说明：",v[0].caption.toList()) { item, builder ->
                    builder.td(item)
                }
                table.th().prettyLines("注意：",v[0].notice.toList()) { item, builder ->
                    builder.td(item)
                }
            }
            return table.toString()
        }
    }
}