package bot_luo_core.cli.commands

import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.exceptions.CliException
import bot_luo_core.cli.handlers.GreedyMessageArgHandler
import net.mamoe.mirai.message.data.MessageChain
import kotlin.jvm.Throws

@Command(
    name = "dumpfile",
    display = "上传输出",
    alias = ["df"],
    usage = "将命令输出作为文件发送，避免输出消息过长",
    notice = [
        "目前只支持上传群文件"
    ]
)
class DumpFileCmd(context: CmdContext) : Cmd(context) {

    @Throws(CliException::class)
    @Method(name = "", alias = [], pmsLevel = CmdPermissionLevel.OP)
    fun dump (
        @Argument(name = "命令", handler = GreedyMessageArgHandler::class)
        cmd: MessageChain
    ): CmdReceipt { with(context) {
        val sub = context.fork(reader = MessageReader(cmd))
        CmdHandler.execute(sub)
        print(sub.getOutput())
        return SUCCESS(uploadFile = 2)
    } }

}