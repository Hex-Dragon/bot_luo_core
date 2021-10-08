package bot_luo_core.cli.commands

import bot_luo_core.bot.BotLuo
import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.checkers.BOT_RUNNING
import bot_luo_core.cli.checkers.BotRunningChecker
import bot_luo_core.cli.handlers.MultiGroupArgHandler
import bot_luo_core.data.Group
import bot_luo_core.data.Groups
import bot_luo_core.data.Users
import bot_luo_core.util.TableBuilder

@Command(
    name = "control",
    display = "控制",
    alias = ["ctrl"],
    usage = "控制类命令组，约等于Ctrl键"
)
class ControlCmd(context: CmdContext) : Cmd(context) {

    @Method(name = "s", alias = [], pmsLevel = CmdPermissionLevel.OP, title = "保存")
    fun save (): CmdReceipt {
        Users.saveAll()
        Groups.saveAll()
        context.print("数据已保存")
        return SUCCESS
    }

    @Method(name = "p", alias = [], pmsLevel = CmdPermissionLevel.OP, ignoreCheckers = [BotRunningChecker::class], title = "暂停")
    fun pause (): CmdReceipt {
        BOT_RUNNING = !BOT_RUNNING
        context.print("运行中：$BOT_RUNNING")
        return SUCCESS
    }

    @Method(name = "m", alias = [], pmsLevel = CmdPermissionLevel.OP, title = "查看映射")
    fun map (
        @Argument(name = "群组", required = false, multiValued = true, handler = MultiGroupArgHandler::class)
        groupsIn: Collection<Group>?
    ): CmdReceipt {
        val groups = groupsIn ?: listOf(context.group)
        val table = TableBuilder(4)
        table.prettyLines("伯特映射 ——", groups) { group, builder ->
            val bot = BotLuo.getMainBot(group.id)
            builder.td("${group.name}(${group.id})").td("${bot?.nick}(${bot?.id})")
        }
        context.print(table.toString())
        return SUCCESS
    }

//    @Method(name = "z", alias = [], pmsLevel = CmdPermissionLevel.HIGH, title = "撤消")
//    fun undo (): CmdReceipt {
//
//    }
//
//    @Method(name = "y", alias = [], pmsLevel = CmdPermissionLevel.HIGH, title = "重做")
//    fun redo (): CmdReceipt {
//
//    }

}