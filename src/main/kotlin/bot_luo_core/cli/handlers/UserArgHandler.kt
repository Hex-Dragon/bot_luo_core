package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.ContextNeeded
import bot_luo_core.cli.exceptions.EmptyTag
import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.cli.exceptions.TagMultiValued
import bot_luo_core.data.User
import bot_luo_core.data.UserTags
import bot_luo_core.data.Users
import kotlin.reflect.KType

/**
 * 可以处理QQ号，At，和以#开头的单值标签
 */
class UserArgHandler: ArgHandler<User> {

    override val name = "用户参数解析器"

    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?): User {
        reader.skipWhitespace()
        val at = reader.readAt()
        if (at != null) return Users.readUser(at.target)

        if (reader.canRead() && reader.peek() == '#') {
            val tag = reader.readString()
            val res = UserTags.readUserTag(tag, context) ?: throw ContextNeeded(pos, argName)
            when (res.size) {
                0 -> throw EmptyTag(pos, tag)
                1 -> return  res[0]
                else -> throw TagMultiValued(pos, tag)
            }
        }

        val input = reader.readString()
        val id = input.toLongOrNull() ?: throw HandlerFatal(input, argName, pos, type)
        return Users.readUser(id)
    }
}