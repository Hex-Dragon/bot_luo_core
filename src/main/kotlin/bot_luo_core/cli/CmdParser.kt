package bot_luo_core.cli

import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.exceptions.*
import bot_luo_core.data.Config.CMD_PREFIX
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.message.data.isContentEmpty
import kotlin.jvm.Throws
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance

/**
 * # 命令解析器类
 */
class CmdParser(private val reader: MessageReader) {

    private val pos: Int = reader.getCursor()
    private val argPos: Int
    val cmdExList: List<CmdExecutable>

    init {
        val cmdName: String
        val methName: String
        if (reader.peek() in CMD_PREFIX) reader.skip()
        if (reader.canRead() && reader.isWhitespace(reader.peek())) reader.skip()
        val head = reader.readStringUntilWhiteSpace()
        val index = head.indexOf("-")
        if (index == -1) {
            cmdName = head
            methName = ""
        } else {
            cmdName = head.substring(0, index)
            methName = head.substring(index + 1)
        }
        cmdExList = CmdCatalog.findCmdExOrNull(cmdName, methName) ?: throw NoCmdFound(head, pos)
        argPos = reader.getCursor()
    }

    fun reset() {
        reader.setCursor(argPos)
    }

    fun finish() {
        val pos = reader.getCursor()
        val rem = reader.readRemainingMessage()
        if (!rem.isContentEmpty()) throw SurplusArg(pos, rem.content)
    }

    @Throws(CmdParseFatal::class)
    fun parse(param: Argument, targetType: KType, context: CmdContext): Any? {
        reader.skipWhitespace()
        val pos = reader.getCursor()

        val ins = param.handler.createInstance()
        if (reader.canRead()) {
            if (param.multiValued) {
                val res = ArrayList<Any?>()
                if (reader.isMultipleStringStart(reader.peek())) {
                    for (m in reader.readMultipleMessage()) {
                        val value = ins.handle(MessageReader(m), pos, param.display, targetType.arguments[0].type, context)
                        if (ins.multiValued) {
                            res.addAll(value as Collection<Any?>)
                        } else {
                            res.add(value)
                        }
                    }
                } else {
                    val value = ins.handle(reader, pos, param.display, targetType.arguments[0].type, context)
                    if (ins.multiValued) {
                        res.addAll(value as Collection<Any?>)
                    } else {
                        res.add(value)
                    }
                }
                return res
            } else {
                return ins.handle(reader, pos, param.display, targetType, context)
            }
        } else if (!param.required) {
            return null
        } else throw NoMoreArg(pos, formatParameter(param))
    }
}