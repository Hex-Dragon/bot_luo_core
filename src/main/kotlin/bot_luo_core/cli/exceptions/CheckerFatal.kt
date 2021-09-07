package bot_luo_core.cli.exceptions

import bot_luo_core.cli.Checker
import net.mamoe.mirai.message.data.Message
import kotlin.reflect.KClass

/**
 * 检定器检定失败
 */
class CheckerFatal(
    override val output: Message,
    override val message: String,
    val checker: KClass<out Checker>
    ) : CliException() {
    override val level = CliExceptionLevel.INFO
}