package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.HandlerFatal
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.isContentEmpty
import kotlin.reflect.KType

class GreedyMessageArgHandler: ArgHandler<MessageChain> {

    override val name = "贪婪消息参数解析器"

    override fun handle(
        reader: MessageReader,
        pos: Int,
        argName: String?,
        type: KType?,
        context: CmdContext?
    ): MessageChain {
        val msg = reader.readRemainingMessage()
        if (msg.isContentEmpty()) throw HandlerFatal(msg.content, argName, pos, type)
        else return msg
    }
}