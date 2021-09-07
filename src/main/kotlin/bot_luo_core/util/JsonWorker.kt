package bot_luo_core.util

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.TypeReference
import java.io.File
import java.lang.ClassCastException
import kotlin.jvm.Throws


/**
 * # JSON数据读写工具类
 */
object JsonWorker {

    @Throws(ClassCastException::class)
    inline fun <reified T> readJson (filePath: String): T?{
        val file = File(filePath)
        if (!file.exists()) {
            if (!file.parentFile.exists())
                file.parentFile.mkdirs()
            file.createNewFile()
            return null
        }
        val typeRef = object : TypeReference<T>() {}
        return JSON.parseObject(file.readText(),typeRef)
    }

    fun writeJson (filePath: String, obj: Any) = File(filePath).writeText(JSON.toJSONString(obj, true))

}