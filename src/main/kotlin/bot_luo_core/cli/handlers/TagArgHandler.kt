package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.SyntaxError
import java.util.*
import kotlin.reflect.KType

class TagArgHandler: ArgHandler<String> {

    override val name = "标签参数处理器"

    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?): String {
        val tag = reader.readString()
        if (!Regex("#[a-zA-Z0-9_-]+").matches(tag)) throw SyntaxError(pos, "标签结构错误或包含非法字符")
        return tag.lowercase(Locale.getDefault())
    }
}