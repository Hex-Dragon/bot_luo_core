package bot_luo_core.util

import com.alibaba.fastjson.JSON
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.content
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

    fun String.escapeJson(): String = JSON.toJSONString(this@escapeJson)

    infix operator fun String.times(n: Int): String {
        val sb = StringBuilder()
        repeat(n) { sb.append(this) }
        return sb.toString()
    }

    infix operator fun Char.times(n: Int): String {
        val sb = StringBuilder()
        repeat(n) { sb.append(this) }
        return sb.toString()
    }

    fun String.firstNotWhitespace(max: Int = this.length): Char? {
        for (i in 0 until max) {
            if (!Character.isWhitespace(this[i])) return this[i]
        }
        return null
    }

    fun String.toLowercase(): String = this.lowercase(Locale.getDefault())

    /**
     * 将此字符串视为MiraiCode进行解码并获取其内容
     */
    val String.miraiCodeContent: String get() = this.deserializeMiraiCode().content
}