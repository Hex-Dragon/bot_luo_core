package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.ContextNeeded
import bot_luo_core.cli.exceptions.EmptyTag
import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.cli.exceptions.TagMultiValued
import bot_luo_core.data.Group
import bot_luo_core.data.Groups
import bot_luo_core.data.Tags
import kotlin.reflect.KType

class GroupArgHandler: ArgHandler<Group> {

    override val name = "群组参数解析器"

    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?): Group {
        reader.skipWhitespace()

        if (reader.canRead() && reader.peek() == '#') {
            val tag = reader.readString()
            val res = Tags.readGroupTag(tag, context) ?: throw ContextNeeded(pos, argName)
            when (res.size) {
                0 -> throw EmptyTag(pos, tag)
                1 -> return  res[0]
                else -> throw TagMultiValued(pos, tag)
            }
        }

        val input = reader.readString()
        val id = input.toLongOrNull()?: throw HandlerFatal(input, argName, pos, type)
        return Groups.readGroup(id)
    }
}