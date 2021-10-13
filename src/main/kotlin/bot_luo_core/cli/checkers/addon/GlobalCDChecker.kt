package bot_luo_core.cli.checkers.addon

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable
import bot_luo_core.data.Cmds
import bot_luo_core.util.Time
import net.mamoe.mirai.message.data.toPlainText

class GlobalCDChecker: Checker {

    override val name = "命令冷却检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) { with(context) {
        val cd = Cmds.getObj<Long>("${cmd.id}_CD") ?: 0
        val wait = cd + Cmds[cmd].lastTime - time
        if (cd > 0 && wait > 0) throw CheckerFatal(
            "等${Time.formatSpanE(wait)}吧，命令全局冷却中".toPlainText(),
            "命令全局冷却中"
        )
    } }
}