package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.CliInternalError
import bot_luo_core.cli.exceptions.HandlerFatal
import kotlin.reflect.KType

class LiteralArgHandler: ArgHandler<Unit> {

    override val name = "字面量参数解析器"

    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?) {
        argName?: throw CliInternalError("缺少字面量名称")
        val input = reader.readUnquotedString()
        if (input != argName) throw HandlerFatal(input, argName, pos, type)
    }
}