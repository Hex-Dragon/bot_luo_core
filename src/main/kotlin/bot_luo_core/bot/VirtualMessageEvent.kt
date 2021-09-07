package bot_luo_core.bot

import bot_luo_core.cli.CmdContext
import bot_luo_core.data.Group
import bot_luo_core.data.User
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.message.data.MessageChain

class VirtualMessageEvent(
    val context: CmdContext
): AbstractEvent() {
    val message: MessageChain get() = context.reader.original
    val user: User get() = context.user
    val group: Group get() = context.group
}
