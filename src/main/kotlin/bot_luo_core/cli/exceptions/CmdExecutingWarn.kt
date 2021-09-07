package bot_luo_core.cli.exceptions

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

/**
 * 命令执行错误，通常由执行命令时接受到命令内部抛出的非[CliException]错误转换得到
 */
class CmdExecutingWarn(override val output: Message, override val message: String) : CliException() {
    override val level = CliExceptionLevel.WARN

    constructor(e: Throwable): this (
        "命令执行中发生意外：\n${e::class.simpleName}".toPlainText(),
        "[WARN]发生错误：${e.message}"
    )
}