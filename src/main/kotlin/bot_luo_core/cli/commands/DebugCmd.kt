package bot_luo_core.cli.commands

import bot_luo_core.bot.BotLuo
import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.data.Data
import bot_luo_core.data.Groups
import bot_luo_core.data.Users
import bot_luo_core.util.TableBuilder
import bot_luo_core.util.Text.limitStart
import bot_luo_core.util.Time.relativeTo
import java.text.DecimalFormat

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
        table.tr("start_at:").td( BotLuo.startAt relativeTo context.time)
        table.tr("mem_use:").td(formatMem(Runtime.getRuntime().totalMemory()))
        table.th()
        table.prettyLines("active_groups(${Groups.activeGroupsCount}):", Groups.groups.keys) { item, builder ->
            builder.td(item)
        }
        table.th()
        table.prettyLines("active_users(${Users.activeUsersCount}):", Users.users.keys) { item, builder ->
            builder.td(item)
        }
        table.th()
        table.prettyLines("saving_data(${Data.savingJobs.size}):",Data.savingJobs.keys) { item, builder ->
            builder.td(item::class.simpleName).td(item.filePath.limitStart(20))
        }
        context.print(table.toString())
        return SUCCESS
    }

    companion object {
        fun formatMem(sizeIn: Long): String {
            val format = DecimalFormat(",###.##")
            var size = sizeIn.toDouble()
            if (size < 1024) return format.format(size) + "B"
            size /= 1024
            if (size < 1024) return format.format(size) + "KB"
            size /= 1024
            if (size < 1024) return format.format(size) + "MB"
            size /= 1024
            return format.format(size) + "GB"
        }
    }
}
