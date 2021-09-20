package bot_luo_core.cli.exceptions

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.ERROR

/**
 * 命令执行错误，通常由执行命令时接受到命令内部抛出的非[CliException]错误转换得到
 */
class CmdExecutingError(override val output: Message, override val message: String, override val  targetException: Throwable) : CliException(), CliTargetException {
    override val logLevel: Level = ERROR

    constructor(e: Throwable): this (
        "命令执行中发生错误：\n${e::class.simpleName}".toPlainText(),
        "发生错误：${e::class.simpleName}: ${e.message}\n${e.stackTraceToString()}",
        e
            )
}