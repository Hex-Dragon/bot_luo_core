package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.ContextNeeded
import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.cli.exceptions.SyntaxError
import bot_luo_core.util.Time
import java.text.ParseException
import java.util.*
import kotlin.reflect.KType

class TimeArgHandler: ArgHandler<Long> {

    override val name = "时间参数解析器"

    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?): Long {
        if (reader.canRead() && reader.peek()=='~') {
            context?: throw ContextNeeded(pos, "~")
            reader.skip()
            if (!reader.canRead()) return context.time
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"))
            val back = reader.peek() == '-'
            if (reader.peek() in "-+") reader.skip()
            var i = 0
            while (reader.canRead() && !Character.isWhitespace(reader.peek())) {
                when (val c = reader.read()) {
                    in '0'..'9' -> {
                        i *= 10
                        i += c - '0'
                    }
                    'd' -> {
                        if (back) i *= -1
                        calendar.add(Calendar.DAY_OF_MONTH,i)
                        i = 0
                    }
                    'h' -> {
                        if (back) i *= -1
                        calendar.add(Calendar.HOUR_OF_DAY,i)
                        i = 0
                    }
                    'm' -> {
                        if (back) i *= -1
                        calendar.add(Calendar.MINUTE,i)
                        i = 0
                    }
                    's' -> {
                        if (back) i *= -1
                        calendar.add(Calendar.SECOND,i)
                        i = 0
                    }
                    else -> throw SyntaxError(pos,"无效的时间单位 $c")
                }
            }
            return calendar.timeInMillis + if (back) -i else i
        } else {
            val str = reader.readString()
            try {
                return Time.parse(str)
            } catch (e: ParseException) {
                throw HandlerFatal(str, argName, pos, type)
            }

        }
    }
}