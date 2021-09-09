package bot_luo_core.data

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.io.File

/**
 * # 数据对象类
 *
 * 继承[Mutex]
 *
 * 建议使用[withAccessing]进行读访问，使用[withLockedAccessing]进行读写访问
 */
abstract class DataObj(
    /**
     * JSON数据文件路径
     */
    private val filePath: String,
    /**
     * 自动保存间隔时间(ms)
     *
     * 在访问空闲超过间隔后自动保存
     */
    private val autoSaveInterval: Long,
    /**
     * 是否自动卸载
     *
     * 为`true`时将把读取数据算作访问，且在自动保存后调用[unload]方法
     */
    private val saveAndUnload: Boolean
): Mutex by Mutex() {

    /**
     * 使用[getObj]和[setObj]进行访问
     */
    val jsonObj: JSONObject

    private var saveJob: Job? = null

    /**
     * 访问数量
     *
     * 以此为依据判断是否在活跃，当访问数归零时开始计时尝试保存或卸载
     *
     * 并不能确保线程安全，仅保证保存的数据有效
     *
     * 使用[access]和[]方法更新
     */
    private var accessing = atomic(0)
    private var changed = false

    /**
     * 进行访问，[accessing]加一
     *
     * @see withAccessing
     * @see withLockedAccessing
     */
    fun access() {
        accessing.addAndGet(1)
        saveJob?.cancel()
        saveJob = null
    }

    /**
     * 退出访问，[accessing]减一
     *
     * @see withAccessing
     * @see withLockedAccessing
     */
    fun exit() {
        if ( accessing.addAndGet(-1) == 0 ) {
            launchSaveJob()
        }
    }

    /**
     * 保存兼卸载任务
     */
    private fun launchSaveJob() {
        saveJob?.cancel()
        savingJobs.remove(saveJob)
        saveJob = scope.launch {
            try {
                delay(autoSaveInterval)
            } catch (ignore: CancellationException) {
                return@launch
            }
            if (changed) savingJobs[saveJob]?.start()
            if (saveAndUnload) unload()
        }.apply {
            savingJobs[this] = scope.launch(start = CoroutineStart.LAZY) {save()}
            this.invokeOnCompletion { savingJobs.remove(this) }
        }
    }

    init {
        val file = File(filePath)
        jsonObj = if (file.exists())
            JSON.parseObject(file.readText().ifBlank { "{}" })
        else
            JSONObject(HashMap())
        if (saveAndUnload) launchSaveJob()
    }

    /**
     * 保存前需将有变动的数据使用[setObj]存入[jsonObj]
     *
     * 会自动创建不存在的文件和文件夹
     */
    fun save() {
        val file = File(filePath)
        if (!file.exists()) {
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            file.createNewFile()
        }
        file.writeText(JSON.toJSONString(jsonObj, true))
        changed = false
    }

    /**
     * 移除数据对象
     *
     * 确保卸载时数据已使用[save]存入磁盘
     */
    abstract fun unload()

    /**
     * 是否已有记录文件存在
     */
    fun exists() = File(filePath).exists()

    /**
     * 读取数据，若不存在则返回`null`
     *
     * 自动更新访问时间
     */
    inline fun <reified T> getObj(key: String): T? {
        return if (jsonObj.containsKey(key)) {
            val tr = object : TypeReference<T>() {}
            jsonObj.getObject(key, tr)
        } else null
    }
    inline fun <reified T: Map<*, *>> getObj(): T? {
        val tr = object : TypeReference<T>() {}
        return jsonObj.toJavaObject(tr)
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
        jsonObj[key] = value
        changed = true
    }
    fun <T: Map<out String, *>> setObj(value: T?) {
        jsonObj.fluentClear().fluentPutAll(value)
        changed = true
    }

    companion object {
        val savingJobs = HashMap<Job, Job>()
        val scope = CoroutineScope(Dispatchers.IO)
    }

}

/**
 * 用于记录[DataObj]使用状况，实现自动保存
 *
 * 此方法不对数据加锁
 */
inline fun <T> withAccessing(vararg obj: DataObj, action: ()->T): T {
    obj.forEach { it.access() }
    try {
        return action()
    } finally {
        obj.forEach { it.exit() }
    }
}

/**
 * 用于记录[DataObj]使用状况，实现自动保存
 *
 * 此方法带锁，使用时需避免锁死
 */
suspend inline fun <T> withLockedAccessing(vararg obj: DataObj, owner: Any? = null, action: ()->T): T {
    obj.forEach { it.lock(owner); it.access() }
    try {
        return action()
    } finally {
        obj.forEach { it.exit(); it.unlock(owner) }
    }
}