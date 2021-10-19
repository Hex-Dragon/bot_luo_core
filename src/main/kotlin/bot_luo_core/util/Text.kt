package bot_luo_core.util

import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

object Text {

    fun String.escapeRegex(): String {
        val sb = StringBuilder()
        this@escapeRegex.forEach {
            if (it in  ".\\+*?[^]\$(){}=!<>|:-" ) sb.append('\\')
            sb.append(it)
        }
        return sb.toString()
    }

    fun String.escapeJson(): String = GSON.toJson(this@escapeJson)

    infix operator fun String.times(n: Int): String {
        if (n<=0) return ""
        val sb = StringBuilder()
        repeat(n) { sb.append(this) }
        return sb.toString()
    }

    infix operator fun Char.times(n: Int): String {
        if (n<=0) return ""
        val sb = StringBuilder()
        repeat(n) { sb.append(this) }
        return sb.toString()
    }

    fun String.firstNotWhitespace(max: Int = this.length): Char? {
        for (i in 0 until minOf(max, length)) {
            if (!Character.isWhitespace(this[i])) return this[i]
        }
        return null
    }

    fun String.toLowercase(): String = this.lowercase(Locale.getDefault())

    /**
     * 将此字符串视为MiraiCode进行解码并获取其内容
     */
    val String.miraiCodeContent: String get() = this.deserializeMiraiCode().content

    fun String.encodeUrl(): String = URLEncoder.encode(this, "UTF-8")
    fun String.decodeUrl(): String = URLDecoder.decode(this, "UTF-8")

    /**
     * 限制总长，当超出给定长度时从头部截断
     */
    fun String.limitStart(len: Int): String {
        if (len <= 3) return '.'*len
        return if (this.length > len)
            "..." + this.substring(this.length - len + 3)
        else
            this
    }

    /**
     * 限制总长，当超出给定长度时从尾部截断
     */
    fun String.limitEnd(len: Int): String {
        if (len <= 3) return '.'*len
        return if (this.length > len)
            this.substring(0..len-4) + "..."
        else
            this
    }

    /**
     * 限制总长，当超出给定长度时去除中间
     */
    fun String.limitMid(len: Int): String {
        if (len <= 3) return '.'*len
        return if (this.length > len)
            this.substring(0 until  (len-3)/2) + "..." + this.substring(length - (len-3)/2 until length )
        else
            this
    }

    fun MessageChain.limitEnd(len: Int): MessageChain {
        val mcb = MessageChainBuilder()
        var l = 0
        for (m in this) {
            l += m.content.length
            if (l > len) {
                if (m is PlainText) {
                    mcb.add(m.content.limitEnd(len - l + m.content.length))
                }
                break
            }
        }
        return mcb.build()
    }

    fun formatUUID(uuid: String): String {
        return uuid.replace("-", "").let {
            "${it.substring(0..8)}-"
        }
    }
}