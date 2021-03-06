package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.ContextNeeded
import bot_luo_core.cli.exceptions.EmptyTag
import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.data.User
import bot_luo_core.data.UserTags
import bot_luo_core.data.Users
import kotlin.collections.ArrayList
import kotlin.reflect.KType

/**
 * 可以处理QQ号，At，以#开头的标签
 */
class MultiUserArgHandler: ArgHandler<ArrayList<User>> {

    override val name = "多用户参数解析器"

    override val multiValued = true

    override fun handle(
        reader: MessageReader,
        pos: Int,
        argName: String?,
        type: KType?,
        context: CmdContext?
    ): ArrayList<User> {
        reader.skipWhitespace()
        val at = reader.readAt()
        if (at != null) return arrayListOf(Users.readUser(at.target))

        if (reader.canRead() && reader.peek() == '#') {
            val tag = reader.readString()
            val res = UserTags.readUserTag(tag, context) ?: throw ContextNeeded(pos, argName)
            if (res.isNotEmpty())
                return res
            else
                throw EmptyTag(pos, tag)
        }

        val input = reader.readString()
        val id = input.toLongOrNull() ?: throw HandlerFatal(input, argName, pos, type)
        return arrayListOf(Users.readUser(id))
    }
}