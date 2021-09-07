package bot_luo_core.cli

import bot_luo_core.cli.exceptions.SyntaxError
import net.mamoe.mirai.message.data.*
import java.lang.StringBuilder

/**
 * # 消息读取类
 *
 * 工作方式类似StringReader，增加了对多种消息类型的支持
 */
class MessageReader(val original: MessageChain) {

    constructor(msg: Message) : this(msg.toMessageChain())
    constructor(text: String) : this(PlainText(text))
    constructor(reader: MessageReader) : this(reader.original) { setCursor(reader.cursor) }

    private var cursor: Int = 0
    private val index = ArrayList<IndexedMessage>()
    private val string = original.content

    init {
        var i = 0
        for (m in original) {
            index.add(IndexedMessage(i,i+m.content.length,m))
            i += m.content.length
        }
    }

    fun getTotalLen() = string.length

    fun setCursor(c: Int) { cursor = c }
    fun getCursor() = cursor

    fun canRead() = canRead(1)
    fun canRead(len: Int): Boolean = cursor + len <= string.length

    fun peek() = string[cursor]
    fun peek(offset: Int) = string[cursor+offset]

    fun skip() { cursor++ }
    fun skip(len: Int) { cursor+=len }
    fun skipWhitespace() {
        while (canRead() && Character.isWhitespace(peek())) {
            skip()
        }
    }
    fun isWhitespace(c: Char) = Character.isWhitespace(c)
    fun read() = string[cursor++]

    fun getRead() = string.substring(0,cursor)
    fun getRemaining() = string.substring(cursor)
    fun readRemaining(): String {
        val str = getRemaining()
        setCursor(getTotalLen())
        return str
    }
    fun getRemainingMessage(): Message {
        val im = index[getIndex()]
        val mcb = MessageChainBuilder()
        val c = cursor
        if (im.message is PlainText) {
            mcb.add(im.message.content.substring(cursor-im.index))
            cursor = im.next
        }
        while (canRead()) {
            mcb.add(readMessage())
        }
        cursor = c
        return mcb.build()
    }
    fun readRemainingMessage(): MessageChain {
        val im = index.getOrNull(getIndex())?: return EmptyMessageChain
        val mcb = MessageChainBuilder()
        if (im.message is PlainText) {
            mcb.add(im.message.content.substring(cursor-im.index))
            cursor = im.next
        }
        while (canRead()) {
            mcb.add(readMessage())
        }
        return mcb.build()
    }

    fun readStringUntil(terminator: Char): String {
        val result = StringBuilder()
        var escaped = false
        while (canRead()) {
            val c = read()
            if (escaped) {
                escaped = if (c == terminator || c == '\\') {
                    result.append(c)
                    false
                } else {
                    setCursor(getCursor() - 1)
                    throw SyntaxError(getCursor(), "无效的转义")
                }
            } else if (c == '\\') {
                escaped = true
            } else if (c == terminator) {
                return result.toString()
            } else {
                result.append(c)
            }
        }
        throw SyntaxError( getCursor(), "无效的输入")
    }

    fun readStringUntilWhiteSpace(): String {
        val result = StringBuilder()
        while (canRead()) {
            if (!Character.isWhitespace(peek()) && index[getIndex()].message is PlainText)
                result.append(read())
            else
                break
        }
        return result.toString()
    }

    fun readUnquotedString(): String {
        val start = cursor
        while (canRead() && isAllowedInUnquotedString(peek()) && index[getIndex()].message is PlainText) {
            skip()
        }
        return string.substring(start, cursor)
    }

    fun readSingleMultiUnquotedString(): String {
        val start = cursor
        while (canRead() && isAllowedInSingleMultiUnquotedString(peek()) && index[getIndex()].message is PlainText) {
            skip()
        }
        return string.substring(start, cursor)
    }

    fun readSingleMultiUnquotedMessage(): Message {
        return if (index[getIndex()].message is PlainText) {
            val start = cursor
            while (canRead() && isAllowedInSingleMultiUnquotedString(peek())) {
                skip()
            }
            string.substring(start, cursor).toPlainText()
        } else {
            readMessage()
        }
    }

    fun readQuotedString(): String {
        if (!canRead()) {
            return ""
        }
        val next = peek()
        if (!isQuotedStringStart(next)) {
            throw SyntaxError( getCursor(), "非法的引用字符串头")
        }
        skip()
        return readStringUntil(next)
    }

    fun readString(): String {
        if (!canRead()) {
            return ""
        }
        val next = peek()
        if (isQuotedStringStart(next)) {
            skip()
            return readStringUntil(next)
        }
        return readUnquotedString()
    }

    fun readSingleMultiString(): String {
        if (!canRead()) {
            return ""
        }
        val next = peek()
        if (isQuotedStringStart(next)) {
            skip()
            return readStringUntil(next)
        }
        return readSingleMultiUnquotedString()
    }

    fun readSingleMultiMessage(): Message {
        if (!canRead()) {
            return "".toPlainText()
        }
        val next = peek()
        if (isQuotedStringStart(next)) {
            skip()
            return readStringUntil(next).toPlainText()
        }
        return readSingleMultiUnquotedMessage()
    }

    private fun getIndex(): Int {
        var i = 0
        while (index.size > i && index[i].next <= cursor) i++
        return i
    }

    /**
     * 获取当前位置的Message
     *
     * 不影响cursor
     */
    fun peekMessage(): Message {
        val i = getIndex()
        return index[i].message
    }

    /**
     * 读取当前位置的Message
     *
     * 读取成功会将cursor移动到下一个SingleMessage开头
     */
    fun readMessage(): Message {
        val i = getIndex()
        cursor = index[i].next
        return index[i].message
    }

    private inline fun <reified T: Message> readTypedMessage(): T? {
        val i = getIndex()
        return if (index[i].message is T) {
            cursor = index[i].next
            index[i].message as T
        } else null
    }

    fun readPlainText(): PlainText? = readTypedMessage()
    fun readImage(): Image? = readTypedMessage()
    fun readAt(): At? = readTypedMessage()
    fun readAtAll(): AtAll? = readTypedMessage()
    fun readFace(): Face? = readTypedMessage()
    fun readQuoteReply(): QuoteReply? = readTypedMessage()

    /**
     * 读取多值字符串
     *
     * 多值字符串为"()"，"[]"，"{}"包括的使用' ',',','，','\n'分隔的字符串
     * @return 读取到的多个字符串
     * @throws CmdSyntaxException
     */
    fun readMultipleString(): ArrayList<String> {
        val list = ArrayList<String>()
        if (!canRead()) {
            return list
        }
        val end = when (peek()) {
            '(' -> ')'
            '[' -> ']'
            '{' -> '}'
            else -> throw SyntaxError(getCursor(), "非法的多值字符串头")
        }
        skip()
        skipWhitespace()
        while (canRead()) {
            if (peek()==end) {
                skip()
                return list
            } else {
                list.add(readSingleMultiString())
                while (canRead() && peek() in arrayOf(' ',',','，','\n','\r')) skip()
            }
        }
        throw SyntaxError(getCursor(), "多值字符串括号未闭合")
    }

    fun readMultipleMessage(): ArrayList<Message> {
        val list = ArrayList<Message>()
        if (!canRead()) {
            return list
        }
        val end = when (peek()) {
            '(' -> ')'
            '[' -> ']'
            '{' -> '}'
            else -> throw SyntaxError(getCursor(), "非法的多值字符串头")
        }
        skip()
        skipWhitespace()
        while (canRead()) {
            if (peek()==end) {
                skip()
                return list
            } else {
                list.add(readSingleMultiMessage())
                while (canRead() && peek() in arrayOf(' ',',','，','\n','\r')) skip()
            }
        }
        throw SyntaxError(getCursor(), "多值字符串括号未闭合")
    }

    fun isQuotedStringStart(c: Char): Boolean {
        return c == '"' || c == '\''
    }

    fun isMultipleStringStart(c: Char): Boolean {
        return c in "([{"
    }

    fun isAllowedInUnquotedString(c: Char): Boolean {
        return c !in arrayOf(' ','"','\'','\n','\r')
    }

    fun isAllowedInSingleMultiUnquotedString(c: Char): Boolean {
        return c !in arrayOf(' ','"','\'','\n','\r','(',')','[',']','{','}',',','，')
    }

    /**
     * 带起止位置的[SingleMessage]
     *
     * @param next 为下一个Message的[index]
     */
    private data class IndexedMessage(
        val index: Int,
        val next: Int,
        val message: SingleMessage
    ): SingleMessage by message
}