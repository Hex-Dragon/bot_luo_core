package bot_luo_core.data

import bot_luo_core.cli.CmdCatalog.COMMANDS
import bot_luo_core.cli.CmdExecutable
import bot_luo_core.cli.CmdPermissionLevel
import bot_luo_core.util.Logger
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.Level

/**
 * # 权限组数据
 */
object PmsGroups: DataObject("data/pms_groups.json", 10000L, false) {

    private const val DESC_BLOCK = "屏蔽组，所有命令均为禁止状态(-1)"
    private const val DESC_NONE = "虚空组，所有命令均为中立(0)"
    private const val DESC_NORMAL = "普通组，低级命令对此组开启(1)，其余为中立(0)"
    private const val DESC_HIGH = "高级组，高级及以下的命令均开启(1)，其余为中立(0)"
    private const val DESC_OP = "管理组，管理级及以下的命令均开启(1)，其余为中立(0)"
    private const val DESC_DEBUG = "调试组，所有命令均为开启状态(1)"

    val BUILTIN_PMS_GROUPS = arrayOf("BLOCK","NONE","NORMAL","HIGH","OP","DEBUG")

    init {
        Logger.sysLog(Level.DEBUG, "开始构建内建权限组……")
        runBlocking { withLockedAccessing(this@PmsGroups) {
            setObj("BLOCK", PmsGroup("BLOCK", DESC_BLOCK, null, HashMap<String, Int>().apply { putAll(COMMANDS.associate { it.id to -1 })}))
            setObj("NONE", PmsGroup("NONE", DESC_NONE, null, HashMap<String, Int>().apply { putAll(COMMANDS.associate { it.id to 0 })}))
            setObj("NORMAL", PmsGroup("NORMAL", DESC_NORMAL, null, HashMap<String, Int>().apply { putAll(COMMANDS.associate { it.id to if (it.pmsLevel <= CmdPermissionLevel.NORMAL) 1 else 0 })}))
            setObj("HIGH", PmsGroup("HIGH", DESC_HIGH, null, HashMap<String, Int>().apply { putAll(COMMANDS.associate { it.id to if (it.pmsLevel <= CmdPermissionLevel.HIGH) 1 else 0 })}))
            setObj("OP", PmsGroup("OP", DESC_OP, null, HashMap<String, Int>().apply { putAll(COMMANDS.associate { it.id to if (it.pmsLevel <= CmdPermissionLevel.OP) 1 else 0 })}))
            setObj("DEBUG", PmsGroup("DEBUG", DESC_DEBUG, null, HashMap<String, Int>().apply { putAll(COMMANDS.associate { it.id to 1 })}))
        } }
        Logger.sysLog(Level.DEBUG, "内建权限组构建完毕")
    }

    fun PmsGroup.readPmsOn(id: String): Int {
        return this.modify?.get(id)?: get(this.inherit?: "BLOCK").readPmsOn(id)
    }
    fun PmsGroup.readPmsOn(cmd: CmdExecutable) = readPmsOn(cmd.id)

    /**
     * 设置权限组
     *
     * 使用前应先使用[isCyclingRef]检查是否出现循环引用
     */
    operator fun set(name: String, value: PmsGroup?) = setObj(name, value)

    operator fun get(name: String): PmsGroup = getObj(name.uppercase())?: getObj("BLOCK")!!

    fun getOrNull(name: String): PmsGroup? = getObj(name.uppercase())

    /**
     * 检查是否出现循环引用
     *
     * 出现循环引用返回`true`
     */
    fun isCyclingRef(pmsGroup: PmsGroup): Boolean {
        var p = pmsGroup
        while (p.inherit != null) {
            if (p.inherit == pmsGroup.name) return true
            else p = get(p.inherit!!)
        }
        return false
    }
}