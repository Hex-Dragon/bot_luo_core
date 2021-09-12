package bot_luo_core.cli.checkers

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable
import bot_luo_core.util.Time.isSameDayTo
import net.mamoe.mirai.message.data.toPlainText

class UserDayLimitChecker: Checker {

    override val name = "用户当日次数检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) { with(context) {
        val limit = group.getObj<Long>("${cmd.id}_userDayLimit")?: 0
        val data = user.readCmdData(cmd)
        if (limit > 0) {
            if (time isSameDayTo data.lastTime) {
                if (data.dayCount >= limit) throw CheckerFatal(
                    "恁改天吧".toPlainText(),
                    "达到当日次数限制 $limit"
                )
            }
        }
    } }
}