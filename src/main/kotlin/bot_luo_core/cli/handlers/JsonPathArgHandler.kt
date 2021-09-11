package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.cli.exceptions.SyntaxError
import kotlin.reflect.KType

class JsonPathArgHandler: ArgHandler<ArrayList<String>> {

    override val name = "JSON路径参数解析器"

    override fun handle(
        reader: MessageReader,
        pos: Int,
        argName: String?,
        type: KType?,
        context: CmdContext?
    ): ArrayList<String> {
        val res = ArrayList<String>()
        val sb = StringBuilder()
        var open = false
        while (reader.canRead() && !reader.isWhitespace(reader.peek())) {
            when(reader.peek()) {
                in 'a'..'z',
                in 'A'..'Z',
                '-','_'-> {
                    if (open) throw SyntaxError(reader.getCursor(),"期望一个数字 : ${reader.getCursor()}")
                    sb.append(reader.read())
                }
                in '0'..'9' -> sb.append(reader.read())
                '.' -> {
                    if (open) throw SyntaxError(reader.getCursor(),"'['未闭合 : ${reader.getCursor()}")
                    if (sb.isEmpty()) throw SyntaxError(reader.getCursor(),"期望一个文本 : ${reader.getCursor()}")
                    res.add(sb.toString())
                    sb.clear()
                    reader.skip()
                    if (reader.canRead() && (reader.isWhitespace(reader.peek()) || reader.peek() == '['))
                        throw SyntaxError(reader.getCursor(),"期望一个文本 : ${reader.getCursor()}")
                }
                '[' -> {
                    if (open) throw SyntaxError(reader.getCursor(),"不能在索引中使用'[' : ${reader.getCursor()}")
                    open = true
                    if (sb.isNotEmpty()) {
                        res.add(sb.toString())
                        sb.clear()
                    }
                    reader.skip()
                }
                ']' -> {
                    if (!open) throw SyntaxError(reader.getCursor(), "']'无对应的'[' : ${reader.getCursor()}")
                    if (sb.isEmpty()) throw SyntaxError(reader.getCursor(), "索引值不能为空 : ${reader.getCursor()}")
                    open = false
                    reader.skip()
                    if (reader.canRead() && !reader.isWhitespace(reader.peek()) && reader.peek() != '.')
                        throw SyntaxError(reader.getCursor(), "期望一个'.' : ${reader.getCursor()}")
                }
                else -> throw SyntaxError(reader.getCursor(),"无法解析的字符'${reader.peek()}' : ${reader.getCursor()}")
            }
        }
        if (open) throw SyntaxError(reader.getCursor(),"'['未闭合 : ${reader.getCursor()}")
        if (sb.isNotEmpty()) res.add(sb.toString())
        return res
    }
}
