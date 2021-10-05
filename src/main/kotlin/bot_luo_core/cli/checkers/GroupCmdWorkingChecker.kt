package bot_luo_core.cli.checkers

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable

class GroupCmdWorkingChecker: Checker {
    override val name = "群组命令启用检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) {
        if (!context.group.getCmdWorking(cmd)) throw CheckerFatal("此命令未在群组${context.group.name}(${context.group.id})启用")
    }
}