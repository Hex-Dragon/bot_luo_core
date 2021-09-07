package bot_luo_core.cli

import kotlin.jvm.Throws
import kotlin.reflect.KType

interface ArgHandler<T> {
    /**
     * 此解析器是否可能返回多个值
     *
     * 若为`true`，则[T]应为[Collection]<*>，且对应的参数注解必须注明为多值。
     */
    val multiValued: Boolean get() = false
    val name: String

    fun handle(
        reader: MessageReader,
        pos: Int = -1,
        argName: String? = null,
        type: KType? = null,
        context: CmdContext? = null
    ): T
}
