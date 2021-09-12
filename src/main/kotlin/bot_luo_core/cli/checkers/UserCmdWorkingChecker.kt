package bot_luo_core.cli.checkers

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable
import net.mamoe.mirai.message.data.toPlainText

class UserCmdWorkingChecker: Checker {

    override val name = "用户命令启用检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) {
        val data = context.user.readCmdData(cmd)
        if (!data.working) throw CheckerFatal("此命令已对用户 ${context.user.name}(${context.user.id}) 关闭")
    }
}