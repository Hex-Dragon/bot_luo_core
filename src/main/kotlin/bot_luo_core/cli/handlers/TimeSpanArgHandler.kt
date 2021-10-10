package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.SyntaxError
import kotlin.reflect.KType

class TimeSpanArgHandler: ArgHandler<Long> {

    override val name = "时间间隔参数解析器"

    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?): Long {
        var i = 0L
        var j = 0L
        do {
            when (val c = reader.read()) {
                in '0'..'9' -> {
                    i *= 10
                    i += c - '0'
                }
                'd' -> {
                    j += i*24*60*60*1000
                    i = 0
                }
                'h' -> {
                    j += i*60*60*1000
                    i = 0
                }
                'm' -> if (reader.peek()=='s') {
                    j += i
                    i = 0
                    reader.skip()
                } else {
                    j += i*60*1000
                    i = 0
                }
                's' -> {
                    j += i*1000
                    i = 0
                }
                else -> throw SyntaxError(pos,"无效的时间单位 $c")
            }
        } while (reader.canRead() && !Character.isWhitespace(reader.peek()))
        return j+i
    }
}