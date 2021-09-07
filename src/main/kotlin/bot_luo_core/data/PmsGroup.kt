package bot_luo_core.data

import bot_luo_core.cli.CmdPermissionLevel

data class PmsGroup (
    val name: String,
    val description: String,
    /**
     * 基础值，填写[CmdPermissionLevel]的名称或结构为'@'+其他权限组名称的链接
     */
    val base: String,
    val modify: HashMap<String, CmdPermissionLevel>?
)
