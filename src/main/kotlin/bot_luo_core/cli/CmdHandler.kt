package bot_luo_core.cli

import bot_luo_core.bot.VirtualMessageEvent
import bot_luo_core.cli.Checker.Companion.order
import bot_luo_core.cli.exceptions.*
import bot_luo_core.data.Config.CMD_PREFIX
import bot_luo_core.data.Groups
import bot_luo_core.data.Users
import bot_luo_core.data.withAccessing
import bot_luo_core.util.Logger
import bot_luo_core.util.Text.firstNotWhitespace
import kotlinx.coroutines.TimeoutCancellationException
import net.mamoe.mirai.event.events.GroupAwareMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.data.toPlainText
import java.lang.reflect.InvocationTargetException
import kotlin.jvm.Throws
import kotlin.reflect.full.createInstance

object CmdHandler {

    /**
     * ## 消息事件唤起命令
     *
     * 初步过滤消息，若消息以[CMD_PREFIX]开头则发起命令执行，并拦截这个消息事件
     */
    suspend fun call(event: MessageEvent) {
        if (event.message.content.firstNotWhitespace(3) !in CMD_PREFIX) return
        event.intercept()
        val context = CmdContext(
            MessageReader(event.message),
            Users.readUser(event.sender.id),
            if (event is GroupAwareMessageEvent) Groups.readGroup(event.group.id) else Groups.readGroup(0)
        )
        call(context)
    }

    /**
     * ## 虚拟消息事件唤起命令
     *
     * @see call
     */
    suspend fun call(event: VirtualMessageEvent) {
        if (event.message.content.firstNotWhitespace(3) !in CMD_PREFIX) return
        event.intercept()
        call(event.context)
    }

    /**
     * ## 命令上下文唤起命令
     *
     * 捕捉命令执行异常和发送回执消息
     *
     * 有需要时发送命令猜测
     */
    suspend fun call(context: CmdContext) {
        try {
            withAccessing(context.user, context.group) {
                execute(context)
            }
        } catch (e: CliException) {
            if (!e.output.isContentEmpty()) {
                context.sendOutputWithLog(At(context.user.id) + e.output)
            }
        } catch (e: Exception) {
            context.sendOutputWithLog("发生异常：${e::class.simpleName}\n${e.message}".toPlainText())
        }
        context.sendOutputWithLog()
    }

    /**
     * ## 执行命令
     *
     * 对命令发起调用务必使用这个函数
     *
     * 需要对抛出的异常进行处理
     */
    @Throws(CliException::class)
    fun execute(context: CmdContext) { with(context) {
        val parser = CmdParser(reader)
        val parseFatal = ArrayList<CmdParseFatal>()
        val checkerFatal = ArrayList<CheckerFatal>()

        for (cmd in parser.cmdExList) {
            try {
                cmd.checkers.forEach {it.createInstance().check(cmd, context)}
                parser.reset()
                val args = cmd.paramsAnn.mapValues { parser.parse(it.value, it.key.type, context) }
                parser.finish()
                try {
                    context.user.cmdOnExecute(cmd)
                    context.group.cmdOnExecute(cmd)

                    Logger.log(cmd, context, "开始执行")

                    val res = cmd.executeWith(context, args)

                    Logger.log(cmd, context, "执行完成：${res.state}")

                    if (res.state.addCount) {
                        val ud = context.user.readCmdData(cmd)
                        ud.totalCount++
                        ud.dayCount++
                        ud.specialCount++
                        context.user.writeCmdData(cmd, ud)
                        val gd = context.group.readCmdData(cmd)
                        gd.totalCount++
                        gd.dayCount++
                        gd.specialCount++
                        context.group.writeCmdData(cmd, gd)
                    }

                    if (res.state.setTime) {
                        val ud = context.user.readCmdData(cmd)
                        ud.lastTime = context.time
                        context.user.writeCmdData(cmd, ud)
                        val gd = context.group.readCmdData(cmd)
                        gd.lastTime = context.time
                        context.group.writeCmdData(cmd, gd)
                    }
                } catch (e: Exception) {
                    when(e) {
                        is CliException -> throw e
                        is InvocationTargetException -> {
                            when(e.targetException) {
                                //不重要的异常
                                is TimeoutCancellationException,
                                    -> throw CmdExecutingWarn(e.targetException)
                                else -> throw CmdExecutingError(e.targetException)
                            }
                        }
                        else -> throw CliInternalError("调用命令时发送错误：\n${e::class.simpleName}".toPlainText(), "调用命令时发送错误：\n${e::class.simpleName}\n${e.message}")
                    }
                } finally {
                    context.user.cmdFinished(cmd)
                    context.group.cmdFinished(cmd)
                    Logger.log(cmd, context, "命令退出")
                }
                return
            } catch (e: CmdParseFatal) {
                parseFatal.add(e)
            } catch (e: CheckerFatal) {
                checkerFatal.add(e)
            }
        }

        //参数错误或条件错误，选择所有错误中最合适的抛出
        if (parseFatal.isNotEmpty()) {
            throw parseFatal.maxByOrNull { it.pos }!!
        }

        if (checkerFatal.isNotEmpty()) {
            throw checkerFatal.minByOrNull { it.checker.order() }!!
        }
    } }

    /**
     * ## 命令错误后尝试猜测命令
     */
    fun guess(){}
}