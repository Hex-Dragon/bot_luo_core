package bot_luo_core.cli

import bot_luo_core.cli.checkers.*
import bot_luo_core.cli.exceptions.CheckerFatal
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.toPlainText
import kotlin.jvm.Throws
import kotlin.reflect.KClass

interface Checker {

    val name: String

    @Throws(CheckerFatal::class)
    fun check(cmd: CmdExecutable, context: CmdContext)

    @Throws(CheckerFatal::class)
    fun fatal(msg: Message) {
        throw CheckerFatal(msg, "$name 检定失败：${msg.content}", this::class)
    }

    companion object {

        //排序与最终抛出的可能性有关
        val CHECKERS = arrayListOf<KClass<out Checker>>(
            BotRunningChecker::class,
            GroupCmdWorkingChecker::class,
            UserCmdWorkingChecker::class,
            UserParallelExecutingChecker::class,
            PermissionChecker::class
        )

        fun KClass<out Checker>.order() = CHECKERS.indexOf(this)

    }
}
