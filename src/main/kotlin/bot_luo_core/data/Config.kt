package bot_luo_core.data

object Config {
    /**
     * 命令前缀
     */
    val CMD_PREFIX = arrayOf('.','。')

    /**
     * 回复-确认
     */
    val REPL_YES = arrayOf("y","ok","yes","好","是","好的","是的")

    /**
     * 回复-否认
     */
    val REPL_NO = arrayOf("n","no","nop","不","否","不是","不要")

    /**
     * 输出消息最大长度
     */
    const val MAX_OUTPUT_LEN = 1500

    /**
     * 默认静音的[Exception]
     */
    val MUTED_EXCEPTIONS = arrayListOf(
        "NoCmdFound"
    )

    /**
     * 默认静音的[bot_luo_core.cli.Checker]
     */
    val MUTED_CHECKERS = arrayListOf(
        "PermissionChecker",
        "GroupCmdWorkingChecker",
        "UserCmdWorkingChecker",
        "BotRunningChecker"
    )
}