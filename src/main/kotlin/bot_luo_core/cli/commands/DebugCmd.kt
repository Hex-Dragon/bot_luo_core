package bot_luo_core.cli.commands

import bot_luo_core.bot.BotLuo
import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.data.*
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

    @Suppress("UNCHECKED_CAST")
    @Method(name = "", alias = ["show"], pmsLevel = CmdPermissionLevel.DEBUG)
    fun show (): CmdReceipt {
        val table = TableBuilder(2)
        table.th("调试信息 ——").br()
        table.tr("ST:").td( BotLuo.startAt relativeTo context.time)
        table.tr("MEM:").td(formatMem(Runtime.getRuntime().totalMemory()))
        table.th()
        val gs = Groups.groups.clone() as HashMap<Long, Group>
        table.prettyLines("AG(${gs.size}):", gs.values) { item, builder ->
            builder.td("${item.name}(${item.id})")
        }
        table.th()
        val us = Users.users.clone() as HashMap<Long, User>
        table.prettyLines("AU(${us.size}):", us.values) { item, builder ->
            builder.td("${item.name}(${item.id})")
        }
        table.th()
        val sj = Data.savingJobs.clone() as HashMap<out Data, *>
        table.prettyLines("SD(${sj.size}):",sj.keys) { item, builder ->
            if (item.isChanged()) builder.td("*") else builder.td()
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
