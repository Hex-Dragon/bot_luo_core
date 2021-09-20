package bot_luo_core.cli.checkers.addon

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable
import net.mamoe.mirai.message.data.toPlainText

class CmdParallelExecutingChecker: Checker {
    override val name = "全局命令并发检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) {
        if (!context.user.isCmdFree(cmd)) throw CheckerFatal(
            "您呼叫的命令正忙，请稍后再试".toPlainText(),
            "全局有相同命令正在执行"
        )
    }

}