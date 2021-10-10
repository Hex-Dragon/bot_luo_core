package bot_luo_core.data

object Config {
    /**
     * 命令前缀
     */
    val CMD_PREFIX = arrayOf('.','。')

    /**
     * 回复-确认
     */
    val REPL_YES = arrayOf("y","ok","yes","true","好","是","对","行","中","成","好的","是的","对的","没错","确认")

    /**
     * 回复-否认
     */
    val REPL_NO = arrayOf("n","no","nope","false","不","否","孬","不是","不要","爪巴","不对","不好","不行","否认")

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

    /**
     * 主时钟周期(ms)
     */
    val CLOCK_CYCLE = 60_000L
}