package bot_luo_core.data

import bot_luo_core.cli.CmdExecutable

object Cmds: CmdDataObj("data/cmds.json", 10000L, false) {
    override val defaultData get() = CmdData(0, 0, 0, false)

    override fun unload() {}

    operator fun get(id: String): CmdData = readCmdData(id)
    operator fun get(cmd: CmdExecutable) = readCmdData(cmd)
    operator fun set(id: String, value: CmdData) = writeCmdData(id, value)
    operator fun set(cmd: CmdExecutable, value: CmdData) = writeCmdData(cmd, value)
}