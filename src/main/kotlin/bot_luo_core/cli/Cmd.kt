package bot_luo_core.cli

@Suppress("PropertyName", "FunctionName")
abstract class Cmd(val context: CmdContext) {

    val SUCCESS: CmdReceipt get() = CmdReceipt(context.time, CmdExitState.SUCCESS)
    val FATAL: CmdReceipt get() = CmdReceipt(context.time, CmdExitState.FATAL)
    val FATAL_BUT_SET_TIME: CmdReceipt get() = CmdReceipt(context.time, CmdExitState.FATAL_BUT_SET_TIME)
    val FATAL_BUT_ADD_COUNT: CmdReceipt get() = CmdReceipt(context.time, CmdExitState.FATAL_BUT_ADD_COUNT)

    /**
     * @param uploadFile 0:不允许上传 1:超过限制时上传 2:始终上传
     */
    fun SUCCESS(uploadFile: Int) = CmdReceipt(context.time, CmdExitState.SUCCESS, uploadOutputFile = uploadFile)

    /**
     * 命令执行完毕退出前执行
     */
    open fun onExit(receipt: CmdReceipt) {}
}
