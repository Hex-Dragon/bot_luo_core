package bot_luo_core.cli.commands

import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.checkers.BOT_RUNNING
import bot_luo_core.data.Groups
import bot_luo_core.data.Users

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

    @Method(name = "p", alias = [], pmsLevel = CmdPermissionLevel.OP, title = "暂停")
    fun pause (): CmdReceipt {
        BOT_RUNNING = !BOT_RUNNING
        context.print("运行中：$BOT_RUNNING")
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