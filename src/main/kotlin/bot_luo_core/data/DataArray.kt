package bot_luo_core.data

import bot_luo_core.util.GSON
import bot_luo_core.util.clear
import bot_luo_core.util.indices
import com.github.salomonbrys.kotson.removeAll
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.sync.Mutex
import java.io.File

/**
 * # 数据对象类
 *
 * 根对象为一个[JsonArray]
 *
 * 继承[Mutex]
 *
 * 建议使用[withAccessing]进行读访问，使用[withLockedAccessing]进行读写访问
 */
abstract class DataArray(
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
     * 使用[getObj]和[setObj]等进行访问
     *
     * 若直接访问并进行了修改则需要手动调用[markDirty]，否则修改不能被保存
     */
    final override val element: JsonArray

    init {
        val file = File(filePath)
        element = if (file.exists())
            JsonParser().parse(file.readText().ifBlank { "[]" }) as JsonArray
        else
            JsonArray()
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
    inline fun <reified T> getObj(index: Int): T? {
        return if (index >= element.size()) {
            val type = object : TypeToken<T>() {}.type
            GSON.fromJson(element[index], type)
        } else null
    }
    inline fun <reified T: List<*>> getArray(): T {
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
    fun <T> setObj(index: Int, value: T?) {
        element[index] = GSON.toJsonTree(value)
        changed = true
    }
    fun <T: List<*>> setArray(value: T) {
        element.clear()
        value.forEach { v -> element.add(GSON.toJsonTree(v)) }
        changed = true
    }

    fun addObj(any: Any) {
        element.add(GSON.toJsonTree(any))
        changed = true
    }

    fun removeObj(index: Int): JsonElement? {
        changed = true
        return if (index in element.indices)
            element.remove(index)
        else null
    }
}