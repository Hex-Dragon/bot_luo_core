package bot_luo_core.cli.checkers

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable
import net.mamoe.mirai.message.data.toPlainText

class GroupCmdWorkingChecker: Checker {

    override val name = "群组命令启用检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) {
        val data = context.group.readCmdData(cmd)
        if (!data.working) fatal("此命令未在群组 ${context.group.name}(${context.group.id}) 启用".toPlainText())
    }
}