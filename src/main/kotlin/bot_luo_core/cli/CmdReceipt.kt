package bot_luo_core.cli

import java.util.*
import kotlin.collections.ArrayList

data class CmdReceipt(
    val time: Long,
    val state: CmdExitState,
    val messages: ArrayList<String> = ArrayList(),
    val f: String? = null,
    val b: String? = null
) {
    /**
     * 允许撤销操作
     */
    fun undoable(): Boolean {
        return f!=null && b!=null
    }

    /**
     * 需要保存
     */
    fun needSave(): Boolean {
        return state.setTime || state.addCount
    }
}