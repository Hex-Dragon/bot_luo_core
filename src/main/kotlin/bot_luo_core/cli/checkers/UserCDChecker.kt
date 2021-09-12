package bot_luo_core.cli.checkers

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable
import bot_luo_core.util.Time
import net.mamoe.mirai.message.data.toPlainText

class UserCDChecker: Checker {

    override val name = "用户冷却检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) { with(context) {
        val cd = group.getObj<Long>("${cmd.id}_userCD")?: 0
        val wait = cd + user.readCmdData(cmd).lastTime - time
        if (cd > 0 && wait > 0) throw CheckerFatal(
            "恁缓${Time.formatSpanE(wait)}吧".toPlainText(),
            "用户冷却中"
        )
    } }
}