package bot_luo_core.cli.exceptions

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText

/**
 * 内部错误，写出bug了
 */
class CliInternalError(override val output: Message, override val message: String) : CliException() {
    override val level = CliExceptionLevel.ERROR

    constructor(msg: String): this(msg.toPlainText(), msg)
}