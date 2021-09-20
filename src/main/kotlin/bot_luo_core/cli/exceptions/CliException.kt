package bot_luo_core.cli.exceptions

import net.mamoe.mirai.message.data.Message
import org.apache.logging.log4j.Level

/**
 * # CLI异常基类
 *
 * 所有命令行正常解析、执行命令过程中抛出的异常均继承此类
 */
abstract class CliException: Exception() {
    /**
     * 等级
     */
    abstract val logLevel: Level

    /**
     * 输出消息，用于错误信息
     */
    abstract val output: Message

    /**
     * 详细信息，用于记录到log
     */
    abstract override val message: String
}