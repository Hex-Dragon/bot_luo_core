package bot_luo_core.cli.exceptions

import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import kotlin.reflect.KType

/**
 * 命令行解析错误
 */
abstract class CmdParseFatal: CliException() {
    override val level = CliExceptionLevel.INFO

    abstract val pos: Int
}

/**
 * 无匹配命令
 */
class NoCmdFound(input: String, override val pos: Int): CmdParseFatal() {
    override val output = "无效的命令“$input”".toPlainText()
    override val message = "无效的命令“$input”  pos:$pos"
}

/**
 * 解析器解析失败
 */
open class HandlerFatal(
    input: String,
    argName: String?,
    final override val pos: Int,
    val type: KType?
) : CmdParseFatal() {
    override val output: Message = "无法将“${input}”解析为$argName".toPlainText()
    override val message: String = "无法将“${input}”解析为$argName  pos:$pos  type:${type}"
}

/**
 * 语法错误
 */
class SyntaxError(override val pos: Int, msg: ()->Message): CmdParseFatal() {
    override val message: String = msg().toString()
    override val output: Message = msg()

    constructor(pos: Int, msg: String): this(pos, {msg.toPlainText()})
}

/**
 * 缺少上下文
 */
class ContextNeeded(override val pos: Int, argName: String?): CmdParseFatal() {
    override val output: Message = "在解析“$argName”时缺少上下文".toPlainText()
    override val message: String = "在解析“$argName”时缺少上下文  pos:$pos"
}

/**
 * 空标签
 */
class EmptyTag(override val pos: Int, tag: String): CmdParseFatal() {
    override val output = "空的标签“$tag”".toPlainText()
    override val message = "空的标签“$tag”  pos:$pos"
}

/**
 * 标签解析出多值
 */
class TagMultiValued(override val pos: Int, tag: String): CmdParseFatal() {
    override val output = "标签“$tag”指向多个对象".toPlainText()
    override val message = "标签“$tag”指向多个对象  pos:$pos"
}

/**
 * 缺少必要参数
 */
class NoMoreArg(override val pos: Int, argFormat: String): CmdParseFatal() {
    override val output = "缺少参数$argFormat".toPlainText()
    override val message = "缺少参数$argFormat  pos:$pos"
}

/**
 * 未完成的内容
 */
class UncompletedContent(override val pos: Int, argName: String?, detail: String): CmdParseFatal() {
    override val output = "参数“$argName”未完成：$detail".toPlainText()
    override val message = "参数“$argName”未完成：$detail  pos:$pos"
}

/**
 * 多余参数
 */
class SurplusArg(override val pos: Int, input: String?): CmdParseFatal() {
    override val output = "多余的参数“$input”".toPlainText()
    override val message = "多余的参数“$input”  pos:$pos"
}