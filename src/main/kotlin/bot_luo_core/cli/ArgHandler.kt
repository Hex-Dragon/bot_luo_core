package bot_luo_core.cli

import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.cli.exceptions.SyntaxError
import kotlin.reflect.KType

interface ArgHandler<T> {
    /**
     * 此解析器是否可能返回多个值
     *
     * 若为`true`，则[T]应为[Collection]<*>，且对应的参数注解必须注明为多值。
     */
    val multiValued: Boolean get() = false
    val name: String

    /**
     * @throws HandlerFatal （主要）当读入的内容无法转换到需要的格式时抛出。
     *  HandlerFatal提供的信息较简略
     * @throws SyntaxError  （次要）当分析输入时发生语法错误时抛出。
     *  SyntaxError可以提供详细的语法错误信息
     */
    fun handle(
        reader: MessageReader,
        pos: Int = -1,
        argName: String? = null,
        type: KType? = null,
        context: CmdContext? = null
    ): T
}
