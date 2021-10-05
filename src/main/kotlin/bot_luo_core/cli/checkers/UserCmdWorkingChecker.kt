package bot_luo_core.cli.checkers

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable

class UserCmdWorkingChecker: Checker {
    override val name = "用户命令启用检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) {
        if (!context.user.getCmdWorking(cmd)) throw CheckerFatal("此命令未对用户${context.user.name}(${context.user.id})启用")
    }
}