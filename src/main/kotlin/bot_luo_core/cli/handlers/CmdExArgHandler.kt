package bot_luo_core.cli.handlers

import bot_luo_core.cli.*
import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.data.Config.CMD_PREFIX
import kotlin.reflect.KType

class CmdExArgHandler: ArgHandler<List<CmdExecutable>> {

    override val name = "命令方法参数解析器"
    override val multiValued = true

    override fun handle(
        reader: MessageReader,
        pos: Int,
        argName: String?,
        type: KType?,
        context: CmdContext?
    ): List<CmdExecutable> {
        val cmdName: String
        val methName: String
        if (reader.peek() in CMD_PREFIX) reader.skip()
        if (reader.isWhitespace(reader.peek())) reader.skip()
        val head = reader.readStringUntilWhiteSpace()
        val index = head.indexOf("-")
        if (index == -1) {
            cmdName = head
            methName = "*"
        } else {
            cmdName = head.substring(0,index)
            methName = head.substring(index+1)
        }
        return CmdCatalog.matchCmdExOrNull(cmdName, methName)?: throw HandlerFatal(head, argName, pos, type)
    }
}