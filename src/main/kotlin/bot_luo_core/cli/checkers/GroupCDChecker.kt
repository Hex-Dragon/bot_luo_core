package bot_luo_core.cli.checkers

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable
import bot_luo_core.util.Time
import net.mamoe.mirai.message.data.toPlainText

class GroupCDChecker: Checker {

    override val name = "群组冷却检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) { with(context) {
        val cd = group.getObj<Long>("${cmd.id}_groupCD")?: 0
        val wait = cd + group.readCmdData(cmd).lastTime - time
        if (cd > 0 && wait > 0) throw CheckerFatal(
            "等${Time.formatSpanE(wait)}吧，群聊冷却中".toPlainText(),
            "群组冷却中"
        )
    } }
}