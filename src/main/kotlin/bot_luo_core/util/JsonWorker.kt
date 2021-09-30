package bot_luo_core.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
        val type = object : TypeToken<T>() {}.type
        return Gson().fromJson(file.readText(), type)
    }

    fun writeJson (filePath: String, obj: Any) = File(filePath).writeText(Gson().toJson(obj))

}