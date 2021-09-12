package bot_luo_core.cli.checkers

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable
import net.mamoe.mirai.message.data.toPlainText

class UserParallelExecutingChecker: Checker {
    override val name = "用户命令并发检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) {
        if (!context.user.isCmdFree(cmd)) throw CheckerFatal(
            "请等待命令执行完成".toPlainText(),
            "用户有相同命令正在执行"
        )
    }

}