package bot_luo_core.cli.commands

import bot_luo_core.bot.BotLuo
import bot_luo_core.bot.VirtualMessageEvent
import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.checkers.GroupCmdWorkingChecker
import bot_luo_core.cli.handlers.GreedyMessageArgHandler
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.message.data.MessageChain

@Command(
    name = "say",
    display = "说",
    alias = [],
    usage = "模拟在当前上下文中发送消息",
    caption = [
        "可以使用execute指定bot账号发送消息：",
        "  - as  bot  使用指定的bot账号",
        "  - as  #any  使用任意可用bot"
    ]
)
class SayCmd(context: CmdContext) : Cmd(context) {

    @Method(name = "", alias = [], pmsLevel = CmdPermissionLevel.OP, ignoreCheckers = [GroupCmdWorkingChecker::class])
    suspend fun say (
        @Argument(name = "消息", handler = GreedyMessageArgHandler::class)
        msg: MessageChain
    ): CmdReceipt { with(context) {

        if (!group.virtual) {
            if (user.virtual)
                context.sendMessageWithLog(msg)
            else {
                val contact = BotLuo.getContactableBots(group).find { it.id == user.id }?.getGroup(group.id)
                if (contact != null) {
                    group.contact = contact
                    context.sendMessageWithLog(msg)
                }
            }
        }

        VirtualMessageEvent(
            context.fork(reader = MessageReader(msg))
        ).broadcast()

        return SUCCESS
    } }
}