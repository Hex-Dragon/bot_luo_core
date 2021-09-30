package bot_luo_core.data

import bot_luo_core.cli.CmdPermissionLevel

/**
 * # 权限组
 *
 * 一个权限组包含对所有命令方法的启用状态的描述
 */
data class PmsGroup (
    /**
     * 名称
     */
    val name: String,

    /**
     * 描述
     */
    val description: String,

    /**
     * 继承权限组名称，可空
     *
     * 若权限组未最终继承到[bot_luo_core.data.PmsGroups.BUILTIN_PMS_GROUPS]之一，则默认继承`BLOCKED`
     */
    var inherit: String?,

    /**
     * 修改值，可空
     *
     * 修改值将覆盖继承值
     */
    var modify: HashMap<String, Int>?
)
