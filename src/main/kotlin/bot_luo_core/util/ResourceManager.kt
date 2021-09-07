package bot_luo_core.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.contact.AudioSupported
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.jvm.Throws

object ResourceManager {

    private const val imageDir = "data/images/"
    private const val audioDir = "data/audios/"
    fun saveImageRef() = JsonWorker.writeJson(imageDir + "ref_map.json", imgRefMap)
    fun saveAudioRef() = JsonWorker.writeJson(audioDir + "ref_map.json", audRefMap)

    private val imgRefMap: HashMap<String, Int>  = JsonWorker.readJson(imageDir+"ref_map.json")?: HashMap()
    private val audRefMap: HashMap<String, Int>  = JsonWorker.readJson(audioDir+"ref_map.json")?: HashMap()

    private val lock = Mutex()

    /**
     * 将消息链的特殊内容储存于本地，避免消息内容被服务器清理
     *
     * 会检查文件是否已存在，会更新文件引用表
     *
     * 特殊存储的消息有：
     * - [Image]
     * - [OnlineAudio]
     */
    suspend fun MessageChain.downloadResource() {
        lock.withLock {
            this.forEach {
                when (it) {
                    is Image -> {
                        imgRefMap[it.imageId] = imgRefMap[it.imageId]?.inc() ?: 1
                        val file = File(imageDir + it.imageId)
                        if (!file.exists()) downloadHttpFile(it.queryUrl(), file)
                    }
                    is OnlineAudio -> {
                        audRefMap[it.filename] = audRefMap[it.filename]?.inc() ?: 1
                        val file = File(audioDir + it.filename)
                        if (!file.exists()) downloadHttpFile(it.urlForDownload, file)
                    }
                }
            }
            saveImageRef()
            saveAudioRef()
        }
    }

    /**
     * 上传消息链的资源文件
     *
     * **为避免出现意外，应使用函数返回值代替原消息进行发送**
     */
    suspend fun MessageChain.uploadResource(contact: Contact): MessageChain {
        val mcb = MessageChainBuilder()
        lock.withLock {
            this.forEach {
                when (it) {
                    is Image -> {
                        val file = File(imageDir+it.imageId)
                        if (file.exists()) {
                            file.inputStream().use { input ->
                                mcb.add(contact.uploadImage(input.toExternalResource()))
                            }
                        } else {
                            mcb.add("[图片失效]")
                        }
                    }
                    is Audio -> {
                        if (contact is AudioSupported) {
                            val file = File(audioDir + it.filename)
                            if (file.exists()) {
                                file.inputStream().use { input ->
                                    mcb.add(contact.uploadAudio(input.toExternalResource()))
                                }
                            } else {
                                mcb.add("[音频失效]")
                            }
                        } else {
                            mcb.add("[环境不支持音频消息]")
                        }
                    }
                    else -> mcb.add(it)
                }
            }
        }
        return mcb.build()
    }

    /**
     * 删除本地存储的消息链时使用
     *
     * 更新相应文件的引用表，如果无引用则删除文件
     */
    suspend fun MessageChain.deleteResourceIfNeeded() {
        lock.withLock {
            this.forEach {
                when (it) {
                    is Image -> {
                        imgRefMap[it.imageId] = imgRefMap[it.imageId]?.dec() ?: 0
                        if (imgRefMap[it.imageId] == 0) {
                            imgRefMap.remove(it.imageId)
                            File(imageDir+it.imageId).delete()
                        }
                    }
                    is Audio -> {
                        audRefMap[it.filename] = audRefMap[it.filename]?.dec() ?: 0
                        if (audRefMap[it.filename] == 0) {
                            audRefMap.remove(it.filename)
                            File(audioDir+it.filename).delete()
                        }
                    }
                }
            }
            saveImageRef()
            saveAudioRef()
        }
    }

    @Throws(IOException::class)
    fun downloadHttpFile(url: String, file: File) {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connect()
            conn.inputStream.use { input ->
                BufferedOutputStream (FileOutputStream(file)).use { output ->
                    input.copyTo(output)
                }
            }
        } finally {
            conn.disconnect()
        }
    }
}