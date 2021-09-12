package bot_luo_core.data

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import com.alibaba.fastjson.parser.ParserConfig
import com.alibaba.fastjson.util.TypeUtils
import kotlinx.coroutines.sync.Mutex
import java.io.File

/**
 * # 数据对象类
 *
 * 根对象为一个[JSONObject]
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
     * 使用[getObj]和[setObj]进行访问
     */
    val jsonArray: JSONArray

    init {
        val file = File(filePath)
        jsonArray = if (file.exists())
            JSON.parseArray (file.readText().ifBlank { "[]" })
        else
            JSONArray()
        if (saveAndUnload) launchSaveJob()
    }

    /**
     * 保存前需将有变动的数据使用[setObj]存入[jsonArray]
     *
     * 会自动创建不存在的文件和文件夹
     */
    override fun save() {
        val file = File(filePath)
        if (!file.exists()) {
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            file.createNewFile()
        }
        file.writeText(JSON.toJSONString(jsonArray, true))
        changed = false
    }

    /**
     * 读取数据，若不存在则返回`null`
     *
     * 自动更新访问时间
     */
    inline fun <reified T> getObj(index: Int): T? {
        return if (index in jsonArray.indices) {
            val tr = object : TypeReference<T>() {}
            jsonArray.getObject(index, tr)
        } else null
    }
    inline fun <reified T: List<*>> getArray(): T {
        val tr = object : TypeReference<T>() {}
        return jsonArray.toJavaObject(tr)
    }

    fun <T> JSONArray.getObject(index: Int, typeReference: TypeReference<*>?): T {
        val obj: Any = this[index]
        @Suppress("UNCHECKED_CAST")
        return if (typeReference == null) {
            obj as T
        } else TypeUtils.cast(obj, typeReference.type, ParserConfig.getGlobalInstance())
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
        jsonArray[index] = value
        changed = true
    }
    fun <T: List<*>> setArray(value: T) {
        jsonArray.fluentClear().fluentAddAll(value)
        changed = true
    }
}