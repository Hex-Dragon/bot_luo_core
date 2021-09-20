package bot_luo_core.cli.exceptions

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.ERROR

/**
 * 内部错误，写出bug了
 */
class CliInternalError(override val output: Message, override val message: String, override val targetException: Throwable) : CliException(), CliTargetException {
    override val logLevel: Level = ERROR

    constructor(e: Throwable): this(
        "调用命令时发送错误：\n${e::class.simpleName}".toPlainText(),
        "调用命令时发送错误：\n${e::class.simpleName}: ${e.message}\n${e.stackTraceToString()}",
        e
    )
}