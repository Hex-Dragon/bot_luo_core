package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.HandlerFatal
import kotlin.reflect.KType

class GreedyStringArgHandler: ArgHandler<String> {

    override val name = "贪婪字符串参数解析器"

    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?): String {
        val input = reader.readRemaining()
        if (input.isNotBlank()) return input
        else throw HandlerFatal(input, argName, pos,  type)
    }

}