package bot_luo_core.cli.checkers

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdExecutable
import bot_luo_core.data.PmsGroups.readPmsOn
import net.mamoe.mirai.message.data.toPlainText

class PermissionChecker: Checker {

    override val name = "权限检定器"

    override fun check(cmd: CmdExecutable, context: CmdContext) {
        val p1 = context.userF.pmsGroup.readPmsOn(cmd)
        val p2 = context.groupF.pmsGroup.readPmsOn(cmd)
        val b1 = p1 >= cmd.pmsLevel
        val b2 = p2 >= cmd.pmsLevel
        if (!b1 && !b2) throw CheckerFatal("权限不足，要求：${cmd.pmsLevel.name} 群组权限：${p2.name} 用户权限：${p1.name}")
    }

}