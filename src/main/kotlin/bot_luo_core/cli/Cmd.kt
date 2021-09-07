package bot_luo_core.cli

@Suppress("PropertyName")
abstract class Cmd(val context: CmdContext) {

    val SUCCESS: CmdReceipt get() = CmdReceipt(context.time, CmdExitState.SUCCESS)
    val FATAL: CmdReceipt get() = CmdReceipt(context.time, CmdExitState.FATAL)
    val FATAL_BUT_SET_TIME: CmdReceipt get() = CmdReceipt(context.time, CmdExitState.FATAL_BUT_SET_TIME)
    val FATAL_BUT_ADD_COUNT: CmdReceipt get() = CmdReceipt(context.time, CmdExitState.FATAL_BUT_ADD_COUNT)

    /**
     * 命令执行完毕退出前执行
     */
    open fun onExit(receipt: CmdReceipt) {}
}
