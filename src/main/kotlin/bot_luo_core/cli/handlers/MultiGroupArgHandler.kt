package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.ContextNeeded
import bot_luo_core.cli.exceptions.EmptyTag
import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.cli.exceptions.SyntaxError
import bot_luo_core.data.*
import net.mamoe.mirai.message.data.toPlainText
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KType

/**
 * 可以处理群号，以#开头的标签
 */
class MultiGroupArgHandler: ArgHandler<ArrayList<Group>> {

    override val name = "多群组参数解析器"

    override val multiValued = true

    override fun handle(
        reader: MessageReader,
        pos: Int,
        argName: String?,
        type: KType?,
        context: CmdContext?
    ): ArrayList<Group> {
        reader.skipWhitespace()

        if (reader.canRead() && reader.peek() == '#') {
            val tag = reader.readString()
            val res = GroupTags.readGroupTag(tag, context) ?: throw ContextNeeded(pos, argName)
            if (res.isNotEmpty())
                return res
            else
                throw EmptyTag(pos, tag)
        }

        val input = reader.readString()
        val id = input.toLongOrNull() ?: throw HandlerFatal(input, argName, pos, type)
        return arrayListOf(Groups.readGroup(id))
    }
}