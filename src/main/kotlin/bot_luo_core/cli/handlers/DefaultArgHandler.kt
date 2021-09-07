package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.HandlerFatal
import java.util.*
import kotlin.reflect.KType

/**
 * # 默认参数解析器
 *
 * 根据参数的类型自动处理以下类型
 * - [String]
 * - [Int]
 * - [Long]
 * - [Float]
 * - [Double]
 * - [Boolean]
 *
 * 当给定的类型不在以上范围内时读取String并返回
 */
class DefaultArgHandler: ArgHandler<Any> {

    override val name = "默认参数解析器"

    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?): Any {
        if (type == null) return reader.readString()
        val input = reader.readString()
        return when (type.classifier) {
            String::class -> input
            Int::class -> input.toIntOrNull()?:throw HandlerFatal(input, argName, pos, type)
            Long::class -> input.toLongOrNull()?:throw HandlerFatal(input, argName, pos, type)
            Float::class -> input.toFloatOrNull()?:throw HandlerFatal(input, argName, pos, type)
            Double::class -> input.toDoubleOrNull()?:throw HandlerFatal(input, argName, pos, type)
            Boolean::class -> input.lowercase(Locale.getDefault()).toBooleanStrictOrNull()?:throw HandlerFatal(input, argName, pos, type)
            else -> throw HandlerFatal(input, argName, pos, type)
        }
    }

}
