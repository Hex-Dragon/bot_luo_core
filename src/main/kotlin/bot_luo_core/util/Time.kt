package bot_luo_core.util

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object Time {
    var calendar: Calendar = Calendar.getInstance().let { it.timeZone=TimeZone.getTimeZone("GMT+08:00"); it }
    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
    /**
     * 获取当前时间戳
     * @return [Long] 时间戳（毫秒）
     */
    fun time() = System.currentTimeMillis()

    /**
     * 获取当前日期
     */
    fun year(time: Long) = calendar.let { it.timeInMillis= time; it.get(Calendar.YEAR) }
    fun year() = year(time())
    fun month(time: Long) = calendar.let { it.timeInMillis= time; it.get(Calendar.MONTH) }
    fun month() = month(time())
    fun day(time: Long) = calendar.let { it.timeInMillis= time; it.get(Calendar.DAY_OF_MONTH) }
    fun day() = day(time())

    /**
     * 当前时分秒
     */
    fun hour(time: Long) = calendar.let { it.timeInMillis= time; it.get(Calendar.HOUR_OF_DAY) }
    fun hour() = hour(time())
    fun minute(time: Long) = calendar.let { it.timeInMillis= time(); it.get(Calendar.MINUTE) }
    fun minute() = minute(time())
    fun second(time: Long) = calendar.let { it.timeInMillis= time(); it.get(Calendar.SECOND) }
    fun second() = second(time())

    /**
     * 检测时间戳是否在今天
     */
    fun isSameDay(time: Long, timeRef: Long): Boolean {
        val c = Calendar.getInstance()
        c.timeInMillis = time
        c.timeZone = TimeZone.getTimeZone("GMT+08:00")
        val cRef = Calendar.getInstance()
        cRef.timeInMillis = timeRef
        cRef.timeZone = TimeZone.getTimeZone("GMT+08:00")
        return (c.get(Calendar.YEAR) == cRef.get(Calendar.YEAR) && c.get(Calendar.MONTH) == cRef.get(Calendar.MONTH) && c.get(Calendar.DAY_OF_MONTH) == cRef.get(Calendar.DAY_OF_MONTH))
    }

    /**
     * 时间戳（毫秒）转换为可读时间格式
     * @return [String] 时间码字符串
     */
    fun format(time: Long): String = format.format(Date(time))

    /**
     * 解析时间为时间戳（毫秒）
     * @return [Long] 时间戳（毫秒）
     * @throws java.text.ParseException
     */
    fun parse(source: String) = format.parse(source).toInstant().epochSecond*1000

    /**
     * 解析时间（ms）为文本（简略版）
     * @return [String] 时间文本
     */
    fun formatSpanE(time: Long): String{
        var t = time
        val year = t/(1000*60*60*24*365L)
        t%=1000*60*60*24*365L
        val mon = t/(1000*60*60*24*30L)
        t%=1000*60*60*24*30L
        val day = t/(1000*60*60*24)
        t%=1000*60*60*24
        val hour = t/(1000*60*60)
        t%=1000*60*60
        val min = t/(1000*60)
        t%=1000*60
        val sec = t/1000

        return if (year>5)
            "5年以上"
        else if (year in 1 until 5)
            if (mon > 0)
                "${year}年${mon}月"
            else
                "${year}年"
        else if (mon in 4..12)
            "${mon}月"
        else if (mon in 1..3)
            if (day > 0)
                "${mon}月${day}天"
            else
                "${mon}月"
        else if (day in 4..30)
            "${day}天"
        else if (day in 1..3)
            if (hour > 0)
                "${day}天${hour}小时"
            else
                "${day}天"
        else if (hour in 11..60)
            "${hour}小时"
        else if (hour in 1..10)
            if (min > 0)
                "${hour}小时${min}分钟"
            else
                "${hour}小时"
        else if (min in 11..60)
            "${min}分钟"
        else if (min in 1..10)
            if (sec > 0)
                "${min}分钟${sec}秒"
            else
                "${min}分钟"
        else
            "一会"
    }
    /**
     * 解析相对时刻（ms）为文本（简略版）
     * @param [time] 正数为之前，负数为之后
     * @return [String] 时间文本
     */
    fun formatRelativeTimeE(time: Long) = formatSpanE(abs(time)) + if (time>0) "前" else "后"

    /**
     * 解析时间（ms）为文本
     * @return [String] 时间文本
     */
    fun formatSpan(time: Long): String{
        var t = time
        val year = t/(1000*60*60*24*365L)
        t%=1000*60*60*24*365L
        val mon = t/(1000*60*60*24*30L)
        t%=1000*60*60*24*30L
        val day = t/(1000*60*60*24)
        t%=1000*60*60*24
        val hour = t/(1000*60*60)
        t%=1000*60*60
        val min = t/(1000*60)
        t%=1000*60
        val sec = t/1000

        val sb = StringBuilder()
        if (year>0)
            sb.append("${year}年")
        if (mon>0)
            sb.append("${mon}月")
        if (day>0)
            sb.append("${day}天")
        if (hour>0)
            sb.append("${hour}小时")
        if (min>0)
            sb.append("${min}分钟")
        if (sec>0)
            sb.append("${sec}秒")
        return sb.toString()
    }

    /**
     * 解析相对时刻（ms）为文本
     * @param [time] 正数为之前，负数为之后
     * @return [String] 时间文本
     */
    fun formatRelativeTime(time: Long) = formatSpan(abs(time)) + if (time>0) "前" else "后"

    /**
     * 时间差
     */
    infix fun Long.spanFrom(another: Long): String = formatSpan(another - this)

    /**
     * 简略时间差
     */
    infix fun Long.spanFromE(another: Long): String = formatSpanE(another - this)

    /**
     * 相对时间
     */
    infix fun Long.relativeTo(another: Long): String = formatRelativeTime(another - this)

    /**
     * 简略相对时间
     */
    infix fun Long.relativeToE(another: Long): String = formatRelativeTimeE(another - this)
}