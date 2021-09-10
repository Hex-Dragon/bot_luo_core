package bot_luo_core.cli

import bot_luo_core.cli.Checker.Companion.CHECKERS
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.exceptions.CliInternalError
import bot_luo_core.data.Config.CMD_PREFIX
import kotlinx.coroutines.runBlocking
import kotlin.jvm.Throws
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.primaryConstructor

/**
 * # 可执行命令类
 *
 * 包含命令类和命令方法的所有相关信息。
 *
 * 使用[CmdCatalog.registerCmd]方法自动构造此类并添加到命令目录。
 *
 * @param cmd 所属的[命令类][Cmd]
 * @param meth 对应的命令方法函数，类型为[KFunction]<[CmdReceipt]>
 *
 * @property id 结构为[cmdName]-[cmdHead]的字符串，
 */
class CmdExecutable(private val cmd: KClass<out Cmd>, private val meth: KFunction<CmdReceipt>) {

    val cmdAnn: Command by lazy { cmd.findAnnotation()?:throw CliInternalError("命令类 $cmd 未被注解") }
    val methAnn: Method by lazy { meth.findAnnotation()?:throw CliInternalError("命令类 $cmd 中方法 ${meth.name} 未被注解") }
    val paramsAnn: Map<KParameter, Argument> by lazy { meth.parameters.filter { it.kind == KParameter.Kind.VALUE }.associateWith { it.findAnnotation()?: throw CliInternalError("命令类 $cmd 方法 ${meth.name} 参数 ${it.name} 未被注解") } }

    val cmdName = cmdAnn.name
    val cmdHead = listOf(cmdAnn.name, cmdAnn.display, *cmdAnn.alias)
    val methName = methAnn.name
    val methHead = listOf(methAnn.name, *methAnn.alias)
    val order = methAnn.order
    val pmsLevel = methAnn.pmsLevel

    val display = cmdAnn.display
    val id get() = "$cmdName-$methName"
    val idFixed get() = if (methName == "") cmdName else id
    val head get() = CMD_PREFIX[0]+idFixed
    val headPara = when {
        methAnn.alias.isEmpty() -> head
        methName == "" -> "${CMD_PREFIX[0]}$cmdName[-${methAnn.alias.joinToString(",")}]"
        else -> "${CMD_PREFIX[0]}$id(${methAnn.alias.joinToString(",")})"
    }

    val checkers = LinkedHashSet<KClass<out Checker>>().apply{
        addAll(CHECKERS.filter { c -> c !in methAnn.ignoreCheckers })
        addAll(methAnn.addonCheckers)
    }

    val alias = cmdAnn.alias
    val subAlias = methAnn.alias
    val usage = cmdAnn.usage
    val notice = cmdAnn.notice
    val caption = cmdAnn.caption
    val subTitle = methAnn.title
    val subUsage = methAnn.usage
    val simples = methAnn.simples

    @Throws(Exception::class)
    fun executeWith(context: CmdContext, argsIn: Map<KParameter,Any?>): CmdReceipt {
        val ins = cmd.primaryConstructor!!.call(context)
        val args = HashMap<KParameter,Any?>()
        args[meth.instanceParameter!!] = ins
        args.putAll(argsIn)
        val receipt = if (meth.isSuspend) {
            runBlocking {
                meth.callSuspendBy(args)
            }
        } else {
            meth.callBy(args)
        }
        ins.onExit(receipt)
        return receipt
    }

    fun getParasFormat(): String {
        val sb = StringBuilder()
        paramsAnn.values.forEach {
            sb.append(formatParameter(it)).append("  ")
        }
        return sb.toString().trimEnd()
    }

    override fun toString(): String {
        return id
    }
}

fun formatParameter(arg: Argument): String {
    var p = arg.display
    if (arg.multiValued) p += "..."
    if (!arg.literal) p = "<$p>"
    if (!arg.required) p = "[$p]"
    return p
}
