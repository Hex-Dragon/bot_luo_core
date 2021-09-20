package bot_luo_core.cli.exceptions

import bot_luo_core.cli.Checker
import net.mamoe.mirai.message.data.Message
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.INFO
import kotlin.reflect.KClass

/**
 * 检定器检定失败
 */
class CheckerFatal(
    override val output: Message,
    override val message: String,
    val checker: KClass<out Checker>
    ) : CliException() {
    override val logLevel: Level = INFO
}