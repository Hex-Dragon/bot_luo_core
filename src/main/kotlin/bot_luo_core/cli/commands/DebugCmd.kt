package bot_luo_core.cli.commands

import bot_luo_core.bot.BotLuo
import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.data.Data
import bot_luo_core.data.Groups
import bot_luo_core.data.Users
import bot_luo_core.util.TableBuilder
import bot_luo_core.util.Time.relativeTo

@Command(
    name = "debug",
    display = "调试",
    alias = ["dbg","打不过"],
    usage = "显示调试信息，调试命令"
)
class DebugCmd(context: CmdContext) : Cmd(context) {

    @Method(name = "", alias = ["show"], pmsLevel = CmdPermissionLevel.DEBUG)
    fun show (): CmdReceipt {
        val table = TableBuilder(4)
        table.th("调试信息 ——").br()
        table.tr("start_at:").tb( BotLuo.startAt relativeTo context.time)
        table.tr("active_groups:").tb(Groups.activeGroupsCount)
        table.tr("active_users:").tb(Users.activeUsersCount)
        table.tr("save_jobs:").tb(Data.savingJobs.size)
        context.print(table.toString())
        return SUCCESS
    }

}
