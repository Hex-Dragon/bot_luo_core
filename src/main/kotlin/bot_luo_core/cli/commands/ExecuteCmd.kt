package bot_luo_core.cli.commands

import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.checkers.UserCmdParallelExecutingChecker
import bot_luo_core.cli.exceptions.*
import bot_luo_core.cli.handlers.MultiGroupArgHandler
import bot_luo_core.cli.handlers.MultiUserArgHandler
import bot_luo_core.cli.handlers.TimeArgHandler
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.isContentEmpty
import kotlin.reflect.KType

@Command(
    name = "execute",
    display = "执行",
    alias = ["exe"],
    usage = "使用指定的上下文执行命令",
    caption = [
        "子命令列表",
        "as  <用户...>  <子命令>",
        "in  <群组...>  <子命令>",
        "at  <时间>  <子命令>",
        "run  <命令>"
    ]
)
class ExecuteCmd(context: CmdContext) : Cmd(context) {

    @Method(name = "", alias = [], pmsLevel = CmdPermissionLevel.OP, ignoreCheckers = [UserCmdParallelExecutingChecker::class])
    fun run (
        @Argument("子命令", handler = ExecuteArgHandler::class)
        contexts: ArrayList<CmdContext>
    ): CmdReceipt {
        if (contexts.size > MAX_CONTEXTS) {
            context.print("选择的上下文数量超过限制值(${contexts.size}:$MAX_CONTEXTS)")
            return FATAL
        }
        val mcb = MessageChainBuilder()
        mcb.add ("执行输出 ——\n\n")
        var success = 0
        var outputNum = 0
        for (ctx in contexts) {
            try {
                CmdHandler.execute(ctx)
                success++
                val output = ctx.getOutput()
                if (!output.isContentEmpty()) {
                    outputNum++
                    mcb.add ("[I]-U(${ctx.user.id})-G(${ctx.group.id}):{\n")
                    mcb.add (output)
                    mcb.add ("\n}\n")
                }
            } catch (e: CliException) {
                val output = e.output
                if (!output.isContentEmpty()) {
                    outputNum++
                    mcb.add ("[E]-U(${ctx.user.id})-G(${ctx.group.id}):{\n")
                    mcb.add (output)
                    mcb.add ("\n}\n")
                }
            }
        }
        if (outputNum > 0) context.print(mcb.build())
        if (success == 0) return FATAL
        return SUCCESS
    }

    companion object {
        const val MAX_CONTEXTS = 20
    }
}

class ExecuteArgHandler: ArgHandler<ArrayList<CmdContext>> {

    override val name = "execute子命令参数解析器"

    override fun handle(
        reader: MessageReader,
        pos: Int,
        argName: String?,
        type: KType?,
        context: CmdContext?
    ): ArrayList<CmdContext> {
        if (context == null) throw ContextNeeded(pos, argName)
        reader.skipWhitespace()
        val users = arrayListOf(context.user)
        val groups = arrayListOf(context.group)
        var time = context.time
        while (reader.canRead()) {
            when(val sub = reader.readString()) {
                "as" -> {
                    users.clear()
                    reader.skipWhitespace()
                    if (reader.canRead()) {
                        if (reader.isMultipleStringStart(reader.peek())) {
                            for (m in reader.readMultipleMessage()) {
                                users.addAll(MultiUserArgHandler().handle(MessageReader(m), pos, "用户", type, context))
                            }
                        } else {
                            users.addAll(MultiUserArgHandler().handle(reader, pos, "用户", type, context))
                        }
                    } else throw NoMoreArg(pos, "<用户...>")
                }
                "in" -> {
                    groups.clear()
                    reader.skipWhitespace()
                    if (reader.canRead()) {
                        if (reader.isMultipleStringStart(reader.peek())) {
                            for (m in reader.readMultipleMessage()) {
                                groups.addAll(MultiGroupArgHandler().handle(MessageReader(m), pos, "群组", type, context))
                            }
                        } else {
                            groups.addAll(MultiGroupArgHandler().handle(reader, pos, "群组", type, context))
                        }
                    } else throw NoMoreArg(pos, "<群组...>")
                }
                "at" -> {
                    reader.skipWhitespace()
                    time = TimeArgHandler().handle(reader, pos, argName, type, context)
                }
                "run" -> {
                    reader.skipWhitespace()
                    val cmd = reader.readRemainingMessage()
                    if (cmd.isContentEmpty()) throw NoMoreArg(pos, "<命令>")
                    val res = ArrayList<CmdContext>()
                    for (u in users) for (g in groups) {
                        res.add(context.fork(reader = MessageReader(cmd),user = u, group = g, time = time))
                    }
                    return res
                }
                else -> throw HandlerFatal(sub, argName, pos, type)
            }
            reader.skipWhitespace()
        }
        throw UncompletedContent(pos, argName, "缺少run子命令")
    }
}