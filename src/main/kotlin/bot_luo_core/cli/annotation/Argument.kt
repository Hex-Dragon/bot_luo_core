package bot_luo_core.cli.annotation

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.handlers.DefaultArgHandler
import kotlin.reflect.KClass

annotation class Argument (
    /**
     * 显示名称，用于命令参数格式化显示
     */
    val display: String,

    /**
     * 是否为多值参数
     *
     * 为`true`则允许多次使用[handler]解析参数，返回一个[ArrayList]。
     *
     * 若使用多值处理器则必须为`true`，反之不一定
     */
    val multiValued: Boolean = false,

    /**
     * 是否为字面参数
     */
    val literal: Boolean = false,

    /**
     * 是否为必须参数
     *
     * 为`false`时对应的参数应为可null
     */
    val required: Boolean = true,

    /**
     * 参数处理器
     *
     * 若使用的处理器为多值处理器，则[multiValued]必须为`true`
     */
    val handler: KClass<out ArgHandler<*>> = DefaultArgHandler::class
        )