package bot_luo_core.bot

import net.mamoe.mirai.message.MessageReceipt

/**
 * 消息池
 */
object MessagePool {
    private val pool = HashMap<Int, MessageReceipt<*>>()

    fun add(receipt: MessageReceipt<*>): Int {
        val hash = receipt.hashCode()
        pool[hash] = receipt
        return hash
    }


}