package bot_luo_core.cli

import bot_luo_core.bot.VirtualMessageEvent
import bot_luo_core.cli.Checker.Companion.order
import bot_luo_core.cli.commands.DumpFileCmd
import bot_luo_core.cli.exceptions.*
import bot_luo_core.data.*
import bot_luo_core.data.Config.CMD_PREFIX
import bot_luo_core.data.Config.MAX_OUTPUT_LEN
import bot_luo_core.util.Logger
import bot_luo_core.util.Text.firstNotWhitespace
import bot_luo_core.util.Text.limitEnd
import bot_luo_core.util.Time.notSameDayTo
import kotlinx.coroutines.*
import net.mamoe.mirai.event.events.GroupAwareMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.data.toPlainText
import org.apache.logging.log4j.Level
import java.lang.reflect.InvocationTargetException
import kotlin.jvm.Throws
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.createInstance

object CmdHandler {

    /**
     * ## 消息事件唤起命令
     *
     * 初步过滤消息，若消息以[CMD_PREFIX]开头则发起命令执行，并拦截这个消息事件
     */
    suspend fun call(event: MessageEvent): Job? {
        if (event.message.content.firstNotWhitespace(3) !in CMD_PREFIX) return null
        event.intercept()
        val context = CmdContext(
            MessageReader(event.message),
            Users.readUser(event.sender.id).apply { contact = event.sender } ,
            if (event is GroupMessageEvent)
                Groups.readGroup(event.group.id)
            else
                Groups.virtualGroup
        )
        return call(context)
    }

    /**
     * ## 虚拟消息事件唤起命令
     *
     * @see call
     */
    suspend fun call(event: VirtualMessageEvent): Job? {
        if (event.message.content.firstNotWhitespace(3) !in CMD_PREFIX) return null
        event.intercept()
        return call(event.context)
    }

    /**
     * ## 命令上下文唤起命令
     *
     * 捕捉命令执行异常和发送回执消息，有需要时发送命令猜测
     *
     * 每次call会新建一个[CoroutineScope]
     */
    suspend fun call(context: CmdContext): Job { CoroutineScope(Dispatchers.IO).launch {
        try {
            withAccessing(context.user, context.group, Cmds) {
                execute(context)
            }
        } catch (e: Exception) {
            if (e is CliException) {
                when {
                    e::class.simpleName in context.groupF.mutedExceptions ||
                            e::class.allSuperclasses.any { it.simpleName in context.groupF.mutedExceptions } -> {
                    }
                    e is CliTargetException && (e.targetException::class.simpleName in context.groupF.mutedExceptions ||
                            e.targetException::class.allSuperclasses.any { it.simpleName in context.groupF.mutedExceptions }) -> {
                    }
                    e is CheckerFatal && e.checker.simpleName in context.groupF.mutedCheckers -> {
                    }
                    else -> {
                        if (!e.output.isContentEmpty())
                            context.sendOutputWithLog(At(context.user.id) + e.output)
                    }
                }
                Logger.cliLog(e.logLevel, e.message)
            } else {
                context.sendOutputWithLog("发生未处理异常：${e::class.simpleName}".toPlainText())
                Logger.cliError(e, Level.ERROR)
            }
            return@launch
        }
        when (context.uploadOutputFile) {
            0 -> {  //不自动上传
                if (context.getOutput().content.length > MAX_OUTPUT_LEN) {
                    val canDump = try {
                        val cmd = CmdCatalog.findCmdEx("dumpfile","")[0]
                        cmd.checkers.forEach { it.createInstance().check(cmd, context) }
                        true
                    } catch (ignore: CheckerFatal) {
                        false
                    }
                    if (canDump)
                        context.sendOutputWithLog(context.getOutput().limitEnd(MAX_OUTPUT_LEN) + "\n输出过大，考虑使用dumpfile命令获取输出")
                    else
                        context.sendOutputWithLog(context.getOutput().limitEnd(MAX_OUTPUT_LEN))
                } else {
                    context.sendOutputWithLog()
                }
            }
            1 -> {  //长度超出限制时上传
                if (context.getOutput().content.length > MAX_OUTPUT_LEN) {
                    val receipt = context.uploadOutput()
                    if (receipt == null) {
                        context.sendOutputWithLog("输出上传文件失败")
                    }
                } else {
                    context.sendOutputWithLog()
                }
            }
            2 -> {  //始终上传
                val receipt = context.uploadOutput()
                if (receipt == null) {
                    context.sendOutputWithLog("输出上传文件失败")
                }
            }
        }
    }.let { return it } }

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

                    Logger.log(cmd, context, "开始执行", cmd.logLevel)

                    val res = cmd.executeWith(context, args)

                    Logger.log(cmd, context, "执行完成：${res}", cmd.logLevel)

                    runBlocking { withLockedAccessing(context.user, context.group, Cmds) {
                        if (res.addCount) {
                            val ud = context.user.readCmdData(cmd)
                            ud.totalCount++
                            if (time notSameDayTo ud.lastTime) ud.dayCount = 0
                            ud.dayCount++
                            context.user.writeCmdData(cmd, ud)
                            val gd = context.group.readCmdData(cmd)
                            gd.totalCount++
                            if (time notSameDayTo gd.lastTime) gd.dayCount = 0
                            gd.dayCount++
                            context.group.writeCmdData(cmd, gd)
                            val ad = Cmds.readCmdData(cmd)
                            ad.totalCount++
                            if (time notSameDayTo ad.lastTime) ad.dayCount = 0
                            ad.dayCount++
                            Cmds.writeCmdData(cmd, ad)
                        }

                        if (res.setTime) {
                            val ud = context.user.readCmdData(cmd)
                            ud.lastTime = context.time
                            context.user.writeCmdData(cmd, ud)
                            val gd = context.group.readCmdData(cmd)
                            gd.lastTime = context.time
                            context.group.writeCmdData(cmd, gd)
                            val ad = Cmds.readCmdData(cmd)
                            ad.lastTime = context.time
                            Cmds.writeCmdData(cmd, ad)
                        }
                    } }

                    context.uploadOutputFile = maxOf(context.uploadOutputFile, res.uploadOutputFile)
                } catch (e: Exception) {
                    when(e) {
                        is InvocationTargetException -> {
                            when(e.targetException) {
                                is CliException -> throw e  //命令内部抛出的CliException，跳过命令匹配
                                //不重要的异常
                                is TimeoutCancellationException,
                                    -> throw CmdExecutingWarn(e.targetException)
                                else -> throw CmdExecutingError(e.targetException)
                            }
                        }
                        else -> throw CliInternalError(e)
                    }
                } finally {
                    context.user.cmdFinished(cmd)
                    context.group.cmdFinished(cmd)
                    Logger.log(cmd, context, "命令退出", cmd.logLevel)
                }
                return
            } catch (e: InvocationTargetException) {    //此时目标异常一定是Cli异常
                throw e.targetException
            } catch (e: CmdParseFatal) {
                parseFatal.add(e)
            } catch (e: CheckerFatal) {
                checkerFatal.add(e)
            }
        }

        //参数错误或条件错误，选择所有错误中最合适的抛出
        if (parseFatal.isNotEmpty()) {
            val max = parseFatal.maxOf { it.pos }
            val list = parseFatal.filter { it.pos == max }
            if (list.size == 1) throw list[0]
            else throw ParserFatalPackage(max, list)
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