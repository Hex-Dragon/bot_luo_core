package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.HandlerFatal
import java.util.regex.PatternSyntaxException
import kotlin.reflect.KType

class RegexArgHandler: ArgHandler<Regex> {

    override val name = "正则表达式参数解析器"

    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?): Regex {
        val input = reader.readString()
        try {
            return Regex(input)
        } catch (e: PatternSyntaxException) {
            throw HandlerFatal(input, argName, pos, type)
        }
    }
}
