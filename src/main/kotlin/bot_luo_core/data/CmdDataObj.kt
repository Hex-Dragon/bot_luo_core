package bot_luo_core.data

import bot_luo_core.cli.CmdExecutable
import kotlinx.atomicfu.AtomicInt
import kotlinx.atomicfu.atomic

abstract class CmdDataObj(filePath: String): DataObj(filePath) {

    private val cmdData: HashMap<String, CmdData> =  getObj("cmdData")?: HashMap()
    private val cmdHandling = HashMap<CmdExecutable, AtomicInt>()

    abstract val defaultData: CmdData

    fun cmdOnExecute(cmd: CmdExecutable) {
        if (cmdHandling[cmd] == null) cmdHandling[cmd] = atomic(0)
        cmdHandling[cmd]!!.addAndGet(1)
    }

    fun cmdFinished(cmd: CmdExecutable) {
        cmdHandling[cmd]!!.addAndGet(-1)
    }

    fun isCmdFree(cmd: CmdExecutable, accurately: Boolean = false): Boolean {
        return if (accurately) cmdHandling[cmd]?.value?.equals(0) ?: true
        else cmdHandling.filterKeys { it.cmdName == cmd.cmdName }.all {
            it.value.value == 0
        }
    }

    open fun readCmdData(id: String): CmdData {
        return cmdData[id]?: defaultData
    }

    open fun readCmdData(cmd: CmdExecutable) = readCmdData(cmd.id)

    /**
     * 写入命令数据，自动存入jsonObj
     */
    open fun writeCmdData(id: String, data: CmdData) {
        cmdData[id] = data
        setObj("cmdData", cmdData)
    }

    open fun writeCmdData(cmd: CmdExecutable, data: CmdData) = writeCmdData(cmd.id, data)
}