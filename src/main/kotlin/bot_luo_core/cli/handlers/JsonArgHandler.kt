package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.cli.exceptions.SyntaxError
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import kotlin.reflect.KType

class JsonArgHandler: ArgHandler<JsonElement> {

    override val name = "Json参数解析器"

    override fun handle(
        reader: MessageReader,
        pos: Int,
        argName: String?,
        type: KType?,
        context: CmdContext?
    ): JsonElement {
        val sb = StringBuilder()
        var q = 0
        var p = false
        while (reader.canRead()) {
            val c = reader.peek()
            when  {
                !p && c in "{[" -> q++
                !p && c in "}]" -> q--
                p && c=='\\' -> {
                    sb.append(reader.read())
                    if (!reader.canRead()) break
                }
                c == '"' -> p = if (p) {
                    q--
                    false
                } else {
                    q++
                    true
                }
                reader.isWhitespace(c) -> {
                    if (q == 0) break
                }
            }
            sb.append(reader.read())
        }
        val input = sb.toString()
        try {
            return JsonParser().parse(input)
        } catch (e: JsonSyntaxException) {
            throw SyntaxError(pos, e.localizedMessage)
        } catch (e: JsonParseException) {
            throw HandlerFatal(input, argName, pos, type)
        }
    }
}