package bot_luo_core.cli

@Suppress("PropertyName", "FunctionName")
abstract class Cmd(val context: CmdContext) {

    val SUCCESS: CmdReceipt get() = SUCCESS()
    val SUCCESS_NOT_ADD_COUNT: CmdReceipt get() = SUCCESS_NOT_ADD_COUNT()
    val SUCCESS_NOT_SET_TIME: CmdReceipt get() = SUCCESS_NOT_SET_TIME()
    val FATAL: CmdReceipt get() = FATAL()
    val FATAL_BUT_ADD_COUNT: CmdReceipt get() = FATAL_BUT_ADD_COUNT()
    val FATAL_BUT_SET_TIME: CmdReceipt get() = FATAL_BUT_SET_TIME()

    /**
     * 执行成功，增加次数统计，设置时间
     *
     * @param addSpecialCount 增加特殊计次
     * @param uploadFile 0:不允许上传 1:超过限制时上传 2:始终上传
     */
    fun SUCCESS(
        addSpecialCount: Boolean = false,
        uploadFile: Int = 0
    ) = CmdReceipt(
        time = context.time,
        success = true,
        addCount = true,
        setTime = true,
        addSpecialCount = addSpecialCount,
        uploadOutputFile = uploadFile
    )

    /**
     * 执行成功，不增加次数统计，设置时间
     *
     * @param addSpecialCount 增加特殊计次
     * @param uploadFile 0:不允许上传 1:超过限制时上传 2:始终上传
     */
    fun SUCCESS_NOT_ADD_COUNT(
        addSpecialCount: Boolean = false,
        uploadFile: Int = 0
    ) = CmdReceipt(
        time = context.time,
        success = true,
        addCount = false,
        setTime = true,
        addSpecialCount = addSpecialCount,
        uploadOutputFile = uploadFile
    )

    /**
     * 执行成功，增加次数统计，不设置时间
     *
     * @param addSpecialCount 增加特殊计次
     * @param uploadFile 0:不允许上传 1:超过限制时上传 2:始终上传
     */
    fun SUCCESS_NOT_SET_TIME(
        addSpecialCount: Boolean = false,
        uploadFile: Int = 0
    ) = CmdReceipt(
        time = context.time,
        success = true,
        addCount = true,
        setTime = false,
        addSpecialCount = addSpecialCount,
        uploadOutputFile = uploadFile
    )

    /**
     * 执行失败，不增加次数统计，不设置时间
     *
     * @param addSpecialCount 增加特殊计次
     * @param uploadFile 0:不允许上传 1:超过限制时上传 2:始终上传
     */
    fun FATAL(
        addSpecialCount: Boolean = false,
        uploadFile: Int = 0
    ) = CmdReceipt(
        time = context.time,
        success = false,
        addCount = false,
        setTime = false,
        addSpecialCount = addSpecialCount,
        uploadOutputFile = uploadFile
    )

    /**
     * 执行失败，增加次数统计，不设置时间
     *
     * @param addSpecialCount 增加特殊计次
     * @param uploadFile 0:不允许上传 1:超过限制时上传 2:始终上传
     */
    fun FATAL_BUT_ADD_COUNT(
        addSpecialCount: Boolean = false,
        uploadFile: Int = 0
    ) = CmdReceipt(
        time = context.time,
        success = false,
        addCount = true,
        setTime = false,
        addSpecialCount = addSpecialCount,
        uploadOutputFile = uploadFile
    )

    /**
     * 执行失败，不增加次数统计，设置时间
     *
     * @param addSpecialCount 增加特殊计次
     * @param uploadFile 0:不允许上传 1:超过限制时上传 2:始终上传
     */
    fun FATAL_BUT_SET_TIME(
        addSpecialCount: Boolean = false,
        uploadFile: Int = 0
    ) = CmdReceipt(
        time = context.time,
        success = false,
        addCount = false,
        setTime = true,
        addSpecialCount = addSpecialCount,
        uploadOutputFile = uploadFile
    )
}
