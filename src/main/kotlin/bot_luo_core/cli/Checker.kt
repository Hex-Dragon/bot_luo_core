package bot_luo_core.cli

import bot_luo_core.cli.checkers.*
import bot_luo_core.cli.exceptions.CheckerFatal
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import kotlin.jvm.Throws
import kotlin.reflect.KClass

interface Checker {

    val name: String

    @Throws(CheckerFatal::class)
    fun check(cmd: CmdExecutable, context: CmdContext)

    /**
     * 获取一个绑定当前命令的[CheckerFatal]实例
     */
    fun CheckerFatal(output: Message, message: String) = CheckerFatal(output, "$name 检定失败：${message}", this::class)
    /**
     * 获取一个绑定当前命令的[CheckerFatal]实例
     */
    fun CheckerFatal(message: String) = CheckerFatal(message.toPlainText(), message)

    companion object {

        /**
         * ### 默认启用的[Checker]
         *
         * 默认情况下每个[命令方法][CmdExecutable]都会启用包含的Checker，可以在
         * [bot_luo_core.cli.annotation.Method]注解中设置ignoreCheckers忽略不需要的Checker
         *
         * 也可以在[bot_luo_core.cli.annotation.Method]注解中设置addonCheckers附加其他Checker
         */
        val CHECKERS = arrayListOf(
            BotRunningChecker::class,
            GroupCDChecker::class,
            GroupCmdWorkingChecker::class,
            GroupDayLimitChecker::class,
            PermissionChecker::class,
            UserCDChecker::class,
            UserCmdWorkingChecker::class,
            UserDayLimitChecker::class,
            UserCmdParallelExecutingChecker::class
        )

        fun KClass<out Checker>.order() = CHECKERS.indexOf(this)

    }
}
