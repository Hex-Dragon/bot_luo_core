package bot_luo_core.cli.exceptions

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.WARN

/**
 * 命令执行错误，通常由执行命令时接受到命令内部抛出的非[CliException]错误转换得到
 */
class CmdExecutingWarn(override val output: Message, override val message: String, override val targetException: Throwable) : CliException(), CliTargetException {
    override val logLevel: Level = WARN

    constructor(e: Throwable): this (
        "命令执行中发生意外：\n${e::class.simpleName}".toPlainText(),
        "发生错误：${e::class.simpleName}: ${e.message}\n${e.stackTraceToString()}",
        e
    )
}