package bot_luo_core.data

import bot_luo_core.cli.CmdExecutable
import bot_luo_core.cli.CmdPermissionLevel
import bot_luo_core.util.JsonWorker

object PmsGroups {

    private val pmsGroups = HashMap<String, PmsGroup>()
    private const val filePath = "data/pms_groups.json"

    private const val DESC_NONE = "虚空组，无任何命令权限"
    private const val DESC_NORMAL = "普通组，拥有执行最低级命令的权限"
    private const val DESC_HIGH = "高级组，拥有执行较高级命令的权限"
    private const val DESC_OP = "管理组，拥有执行管理命令的权限"
    private const val DESC_DEBUG = "调试组，拥有执行调试中命令的权限"

    init {
        pmsGroups["NONE"] = PmsGroup("NONE", DESC_NONE, CmdPermissionLevel.NONE.name, null)
        pmsGroups["NORMAL"] = PmsGroup("NORMAL", DESC_NORMAL, CmdPermissionLevel.NORMAL.name, null)
        pmsGroups["HIGH"] = PmsGroup("HIGH", DESC_HIGH, CmdPermissionLevel.HIGH.name, null)
        pmsGroups["OP"] = PmsGroup("OP", DESC_OP, CmdPermissionLevel.OP.name, null)
        pmsGroups["DEBUG"] = PmsGroup("DEBUG", DESC_DEBUG, CmdPermissionLevel.DEBUG.name, null)
        JsonWorker.readJson<ArrayList<PmsGroup>?>(filePath)?.forEach { pmsGroups[it.name] = it }
    }

    fun getPmsGroupOrNull(name: String) = pmsGroups[name]
    fun getPmsGroup(name: String) = pmsGroups[name]?: pmsGroups["NONE"]!!

    fun PmsGroup.readPmsOn(id: String): CmdPermissionLevel {
        return this.modify?.get(id)?: if (this.base.startsWith("@")) {
            getPmsGroup(base.substring(1)).readPmsOn(id)
        } else CmdPermissionLevel(this.base)
    }
    fun PmsGroup.readPmsOn(cmd: CmdExecutable) = readPmsOn(cmd.id)

    /**
     * 设置权限组
     *
     * 使用前应先使用[isCyclingRef]检查是否出现循环引用
     */
    fun setPmsGroup(pmsGroup: PmsGroup) {
        pmsGroups[pmsGroup.name] = pmsGroup
        save()
    }

    /**
     * 检查是否出现循环引用
     *
     * 出现循环引用返回`true`
     */
    fun isCyclingRef(pmsGroup: PmsGroup): Boolean {
        var p = pmsGroup
        while (p.base.startsWith('@')) {
            val b = p.base.substring(1)
            if (b == pmsGroup.name) return true
            else p = getPmsGroup(b)
        }
        return false
    }

    private fun save() {
        JsonWorker.writeJson(filePath, pmsGroups.values)
    }

}