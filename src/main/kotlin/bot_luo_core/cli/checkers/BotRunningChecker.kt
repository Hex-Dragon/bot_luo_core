package bot_luo_core.cli.checkers

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable
import net.mamoe.mirai.message.data.toPlainText

class BotRunningChecker: Checker {

    override val name = "机器人运行检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) {
        if (!BOT_RUNNING) fatal("机器人已关闭".toPlainText())
    }

}

var BOT_RUNNING = true