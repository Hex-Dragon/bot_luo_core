package bot_luo_core.cli

import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.commands.*
import bot_luo_core.cli.exceptions.CliInternalError
import bot_luo_core.util.Text.escapeRegex
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

object CmdCatalog {
    val COMMANDS = ArrayList<CmdExecutable>()

    private val ILLEGAL_CHAR = arrayOf('.','*','\"','。','/','\\','[',']','(',')','{','}')

    init {
        //注册内建命令
        registerCmd(
            AnnounceCmd::class,
            ControlCmd::class,
            DebugCmd::class,
            ExecuteCmd::class,
            HelpCmd::class,
            ListCmd::class,
            PermissionCmd::class,
            SayCmd::class,
            SwitchCmd::class,
            TagCmd::class
        )
    }

    /**
     * 注册命令
     */
    fun registerCmd(vararg cmds: KClass<out Cmd>) {
        for (cmd in cmds) {
            for (f in cmd.functions) {
                if (f.hasAnnotation<Method>()) {
                    @Suppress("UNCHECKED_CAST")
                    val cex = CmdExecutable(cmd, f as KFunction<CmdReceipt>)

                    if (cex.cmdHead.any { it.any { c -> c in ILLEGAL_CHAR } }) throw CliInternalError("命令 $cex 中存在不合法的字符")
                    if (cex.methHead.any { it.any { c -> c in ILLEGAL_CHAR } }) throw CliInternalError("命令 $cex 中存在不合法的字符")

                    COMMANDS.add(cex)
                }
            }
        }
    }

    /**
     * 使用字符串查找符合的命令方法
     *
     * 发起命令调用时使用此方法匹配命令头
     */
    fun findCmdEx(cmd: String, meth: String) = COMMANDS.filter { cmd in it.cmdHead && meth in it.methHead }.sortedBy { it.order }

    /**
     * 使用字符串查找符合的命令方法
     *
     * 发起命令调用时使用此方法匹配命令头
     *
     * 若无有效命令返回`null`
     */
    fun findCmdExOrNull(cmd: String, meth: String) = findCmdEx(cmd, meth).ifEmpty { null }

    /**
     * 使用通配符匹配命令方法
     *
     * 使用 `*` 匹配任意长度任意字符
     */
    fun matchCmdEx(cmd: String, meth: String): List<CmdExecutable> {
        val cmdRegex = Regex(cmd.escapeRegex().replace("\\*",".*"))
        val methRegex = Regex(meth.escapeRegex().replace("\\*",".*"))
        return COMMANDS.filter { c -> c.cmdHead.any { cmdRegex.matches(it) } && c.methHead.any { methRegex.matches(it) } }.sortedBy { it.order }
    }

    /**
     * 使用通配符匹配命令方法
     *
     * 无有效命令返回null
     */
    fun matchCmdExOrNull(cmd: String, meth: String) = matchCmdEx(cmd, meth).ifEmpty { null }
}