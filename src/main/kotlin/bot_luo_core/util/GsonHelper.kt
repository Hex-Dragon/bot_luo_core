package bot_luo_core.util

import com.github.salomonbrys.kotson.keys
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.ClassCastException
import kotlin.jvm.Throws

val GSON: Gson get() = GsonBuilder().setPrettyPrinting().create()

operator fun JsonObject.set(key: String, value: Any?) {
    this.add(key, GSON.toJsonTree(value))
}

operator fun JsonArray.set(key: Int, value: Any?) {
    this.set(key, GSON.toJsonTree(value))
}

fun JsonObject.clear() {
    for (k in keys()) remove(k)
}

fun JsonArray.clear() {
    for (i in indices.reversed()) remove(i)
}

val JsonArray.indices get() = 0 until size()

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
        return GSON.fromJson(file.readText(), type)
    }

    fun writeJson (filePath: String, obj: Any) = File(filePath).writeText(GSON.toJson(obj))

}