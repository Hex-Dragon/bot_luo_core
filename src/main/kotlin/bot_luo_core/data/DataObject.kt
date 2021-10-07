package bot_luo_core.data

import bot_luo_core.util.GSON
import bot_luo_core.util.clear
import bot_luo_core.util.set
import com.github.salomonbrys.kotson.put
import com.github.salomonbrys.kotson.putAll
import com.github.salomonbrys.kotson.removeAll
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.sync.Mutex
import java.io.File

/**
 * # 数据对象类
 *
 * 根对象为一个[JsonObject]
 *
 * 继承[Mutex]
 *
 * 建议使用[withAccessing]进行读访问，使用[withLockedAccessing]进行读写访问
 */
abstract class DataObject(
    /**
     * JSON数据文件路径
     */
    filePath: String,
    /**
     * 自动保存间隔时间(ms)
     *
     * 在访问空闲超过间隔后自动保存
     */
    autoSaveInterval: Long,
    /**
     * 是否自动卸载
     *
     * 为`true`时将把读取数据算作访问，且在自动保存后调用[unload]方法
     */
    saveAndUnload: Boolean
): Data(filePath, autoSaveInterval, saveAndUnload) {

    /**
     * 使用[getObj]和[setObj]进行访问
     */
    final override val element: JsonObject

    init {
        val file = File(filePath)
        element = if (file.exists())
            JsonParser().parse(file.readText().ifBlank { "{}" }) as JsonObject
        else
            JsonObject()
        if (saveAndUnload) launchSaveJob()
    }

    /**
     * 保存前需将有变动的数据使用[setObj]存入[element]
     *
     * 会自动创建不存在的文件和文件夹
     */
    override fun save() {
        val file = File(filePath)
        if (!file.exists()) {
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            file.createNewFile()
        }
        file.writeText(GSON.toJson(element))
        changed = false
    }

    /**
     * 读取数据，若不存在则返回`null`
     *
     * 自动更新访问时间
     */
    inline fun <reified T> getObj(key: String): T? {
        return if (element.has(key)) {
            val type = object : TypeToken<T>() {}.type
            GSON.fromJson(element[key], type)
        } else null
    }
    inline fun <reified T: Map<*, *>> getObj(): T {
        val type = object : TypeToken<T>() {}.type
        return GSON.fromJson(element, type)
    }

    /**
     * 写入数据，不触发保存文件
     *
     * 此方法应在[withAccessing]或[withLockedAccessing]代码块中使用
     *
     * 自动设置被更改状态
     *
     * @see withAccessing
     * @see withLockedAccessing
     */
    fun <T> setObj(key: String, value: T?) {
        element[key] = value
        changed = true
    }
    fun <T: Map<String, *>> setObj(value: T?) {
        element.clear()
        value?.forEach{ (k,v) -> element.put(k to GSON.toJsonTree(v)) }
        changed = true
    }
}