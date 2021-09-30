package bot_luo_core.data

import com.google.gson.JsonElement
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.io.File

/**
 * # 数据类
 *
 * 继承[Mutex]
 *
 * 数据读取与写入由继承类实现
 */
abstract class Data(
    /**
     * JSON数据文件路径
     */
    protected val filePath: String,
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
) : Mutex by Mutex() {

    private var saveJob: Job? = null

    abstract val element: JsonElement

    /**
     * 访问数量
     *
     * 以此为依据判断是否在活跃，当访问数归零时开始计时尝试保存或卸载
     *
     * 并不能确保线程安全，仅保证保存的数据有效
     *
     * 使用[access]和[exit]方法更新
     */
    private var accessing: AtomicInt = atomic(0)
    protected var changed: Boolean = false

    /**
     * 进行访问，[accessing]加一
     *
     * @see withAccessing
     * @see withLockedAccessing
     */
    fun access() {
        accessing.addAndGet(1)
        saveJob?.cancel()
        savingJobs.remove(this)
        saveJob = null
    }

    /**
     * 退出访问，[accessing]减一
     *
     * @see withAccessing
     * @see withLockedAccessing
     */
    fun exit() {
        if (accessing.addAndGet(-1) == 0) {
            launchSaveJob()
        }
    }

    /**
     * 保存兼卸载任务
     */
    protected fun launchSaveJob() {
        saveJob?.cancel()
        savingJobs.remove(this)
        saveJob = scope.launch {
            try {
                delay(autoSaveInterval)
            } catch (ignore: CancellationException) {
                return@launch
            }
            if (changed) savingJobs[this@Data]?.invoke()
            if (saveAndUnload) unload()
            savingJobs.remove(this@Data)
        }
        savingJobs[this] =  { save() }
    }

    abstract fun save()

    /**
     * 移除数据
     *
     * 需要确保卸载时数据已使用[save]存入磁盘
     */
    abstract fun unload()

    /**
     * 是否已有记录文件存在
     */
    fun exists() = File(filePath).exists()

    companion object {
        val savingJobs = HashMap<Data, () -> Unit>()
        val scope = CoroutineScope(Dispatchers.IO)
    }

    /**
     * 手动标记这个[Data]被修改过，需要保存
     */
    fun markDirty() {
        changed = true
    }
}

/**
 * 用于记录[Data]使用状况，实现自动保存
 *
 * 此方法不对数据加锁
 */
inline fun <T> withAccessing(vararg obj: Data, action: () -> T): T {
    obj.forEach { it.access() }
    try {
        return action()
    } finally {
        obj.forEach { it.exit() }
    }
}

/**
 * 用于记录[Data]使用状况，实现自动保存
 *
 * 此方法带锁，使用时需避免锁死
 */
suspend inline fun <T> withLockedAccessing(vararg obj: Data, owner: Any? = null, action: () -> T): T {
    obj.forEach { it.lock(owner); it.access() }
    try {
        return action()
    } finally {
        obj.forEach { it.exit(); it.unlock(owner) }
    }
}