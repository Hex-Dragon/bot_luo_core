package bot_luo_core.cli

import kotlin.collections.ArrayList

data class CmdReceipt(
    val time: Long,
    val success: Boolean,
    val addCount: Boolean,
    val setTime: Boolean,
    val messages: ArrayList<String> = ArrayList(),
    val f: String? = null,
    val b: String? = null,
    val uploadOutputFile: Int = 0
) {
    /**
     * 允许撤销操作
     */
    fun undoable(): Boolean {
        return f!=null && b!=null
    }
}