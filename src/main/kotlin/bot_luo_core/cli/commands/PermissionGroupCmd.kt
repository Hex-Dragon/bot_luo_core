package bot_luo_core.cli.commands

import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.cli.exceptions.SyntaxError
import bot_luo_core.cli.handlers.CmdExArgHandler
import bot_luo_core.cli.handlers.GreedyStringArgHandler
import bot_luo_core.cli.handlers.PmsGroupArgHandler
import bot_luo_core.data.PmsGroup
import bot_luo_core.data.PmsGroups
import bot_luo_core.data.PmsGroups.readPmsOn
import bot_luo_core.data.withLockedAccessing
import bot_luo_core.util.TableBuilder
import bot_luo_core.util.Text.limitEnd
import com.github.salomonbrys.kotson.keys
import kotlin.reflect.KType

@Command(
    name = "pmsGroup",
    display = "权限组",
    alias = ["pg"],
    usage = "编辑权限组"
)
class PermissionGroupCmd(context: CmdContext) : Cmd(context) {

    /*  ========================  get  ========================  */

    @Method(name = "", alias = ["get","g"], pmsLevel = CmdPermissionLevel.OP, order = 0, title = "查看")
    fun get (
        @Argument(name = "权限组", handler = PmsGroupArgHandler::class)
        pmsGroup: PmsGroup
    ): CmdReceipt {
        val table = TableBuilder(4)
        table.th("权限组 —— ${pmsGroup.name}")
        table.tr("继承于：").td(pmsGroup.inherit)
        table.prettyLines("修改项：", pmsGroup.modify?.entries?: ArrayList()) { item, builder ->
            builder.td("${item.key}:").td(item.value)
        }
        table.tr("描述：").sp().p(pmsGroup.description)
        context.print(table.toString())
        return SUCCESS
    }

    @Method(name = "", alias = ["get","g"], pmsLevel = CmdPermissionLevel.OP, order = 1, title = "查看")
    fun get (
        @Argument(name = "权限组", handler = PmsGroupArgHandler::class)
        pmsGroup: PmsGroup,
        @Argument(name = "命令", handler = PmsGroupArgHandler::class)
        cmds: List<CmdExecutable>
    ): CmdReceipt {
        val table = TableBuilder(4)
        table.th("权限组 —— ${pmsGroup.name}").br()
        for (cmd in cmds) {
            table.tr("${cmd.id}:").td(pmsGroup.readPmsOn(cmd))
        }
        context.print(table.toString())
        return SUCCESS
    }

    /*  ========================  add  ========================  */

    @Method(name = "add", alias = ["a"], pmsLevel = CmdPermissionLevel.OP, order = 0, title = "新建")
    suspend fun add (
        @Argument(name = "权限组名", handler = PmsGroupNameArgHandler::class)
        name: String,
        @Argument(name = "继承权限组", handler = PmsGroupArgHandler::class)
        inherit: PmsGroup?,
        @Argument(name = "权限组说明", handler = GreedyStringArgHandler::class)
        description: String,
    ): CmdReceipt {
        if (PmsGroups.getOrNull(name) != null) {
            context.print("权限组“$name”已存在")
            return FATAL
        }
        val new = PmsGroup(
            name = name,
            description = description,
            inherit = inherit?.name,
            modify = HashMap()
        )
        if (PmsGroups.isCyclingRef(new)) {
            context.print("权限组继承出现循环")
            return FATAL
        }
        withLockedAccessing(PmsGroups) {
            PmsGroups[name] = new
        }
        val table = TableBuilder(4)
        table.th("新建权限组 —— $name").br()
        table.tr("继承于：").td(new.inherit)
        table.tr("描述：").sp().p(description)
        context.print(table.toString())
        return SUCCESS
    }

    @Method(name = "add", alias = ["a"], pmsLevel = CmdPermissionLevel.OP, order = 1, title = "新建")
    suspend fun add (
        @Argument(name = "权限组名", handler = PmsGroupNameArgHandler::class)
        name: String,
        @Argument(name = "权限组说明", handler = GreedyStringArgHandler::class)
        description: String,
    ): CmdReceipt  = add(name, null, description)

    /*  ========================  modify  ========================  */

    @Method(name = "modify", alias = ["m"], pmsLevel = CmdPermissionLevel.OP, title = "设置修改")
    suspend fun modify (
        @Argument(name = "权限组", handler = PmsGroupArgHandler::class)
        pmsGroup: PmsGroup,
        @Argument(name = "命令", handler = CmdExArgHandler::class)
        cmds: List<CmdExecutable>,
        @Argument(name = "权限值", handler = PmsValueArgHandler::class)
        value: Int
    ): CmdReceipt {
        if (pmsGroup.name in PmsGroups.BUILTIN_PMS_GROUPS) {
            context.print("不允许修改内建权限组")
            return FATAL
        }
        val table = TableBuilder(4)
        table.th("权限组设置修改 —— ${pmsGroup.name}").br()
        if (pmsGroup.modify == null) pmsGroup.modify = HashMap()
        for (cmd in cmds) {
            table.tr(cmd.id).td(pmsGroup.modify!![cmd.id]).td("->").td(value)
            pmsGroup.modify!![cmd.id] = value
        }
        withLockedAccessing(PmsGroups) {
            PmsGroups[pmsGroup.name] = pmsGroup
        }
        context.print(table.toString())
        return SUCCESS
    }

    /*  ========================  removeModify  ========================  */

    @Method(name = "remove-modify", alias = ["rm"], pmsLevel = CmdPermissionLevel.OP, title = "移除修改")
    suspend fun removeModify (
        @Argument(name = "权限组", handler = PmsGroupArgHandler::class)
        pmsGroup: PmsGroup,
        @Argument(name = "命令", handler = CmdExArgHandler::class)
        cmds: List<CmdExecutable>
    ): CmdReceipt {
        if (pmsGroup.name in PmsGroups.BUILTIN_PMS_GROUPS) {
            context.print("不允许修改内建权限组")
            return FATAL
        }
        val table = TableBuilder(4)
        table.th("权限组移除修改 —— ${pmsGroup.name}").br()
        if (pmsGroup.modify == null) {
            context.print("权限组“${pmsGroup.name}”未设置任何修改")
            return FATAL
        }
        for (cmd in cmds) {
            table.tr(cmd.id).td(pmsGroup.modify!![cmd.id]).td("->").td("null")
            pmsGroup.modify!!.remove(cmd.id)
        }
        withLockedAccessing(PmsGroups) {
            PmsGroups[pmsGroup.name] = pmsGroup
        }
        context.print(table.toString())
        return SUCCESS
    }

    /*  ========================  modifyInherit  ========================  */

    @Method(name = "modify-inherit", alias = ["mi"], pmsLevel = CmdPermissionLevel.OP, title = "修改继承")
    suspend fun modifyInherit (
        @Argument(name = "权限组", handler = PmsGroupArgHandler::class)
        pmsGroup: PmsGroup,
        @Argument(name = "继承权限组", required = false, handler = PmsGroupArgHandler::class)
        inherit: PmsGroup?,
    ): CmdReceipt {
        if (pmsGroup.name in PmsGroups.BUILTIN_PMS_GROUPS) {
            context.print("不允许修改内建权限组")
            return FATAL
        }
        val table = TableBuilder(4)
        table.th("权限组修改继承 —— ${pmsGroup.name}").br()
        table.tr(pmsGroup.inherit).td("->").td(inherit?.name)
        pmsGroup.inherit = inherit?.name
        withLockedAccessing(PmsGroups) {
            PmsGroups[pmsGroup.name] = pmsGroup
        }
        context.print(table.toString())
        return SUCCESS
    }

    /*  ========================  remove  ========================  */

    @Method(name = "remove", alias = ["r"], pmsLevel = CmdPermissionLevel.OP, title = "删除")
    suspend fun remove (
        @Argument(name = "权限组", handler = PmsGroupArgHandler::class)
        pmsGroup: PmsGroup
    ): CmdReceipt {
        if (pmsGroup.name in PmsGroups.BUILTIN_PMS_GROUPS) {
            context.print("不允许修改内建权限组")
            return FATAL
        }
        context.sendOutputWithLog("是否要删除权限组“${pmsGroup.name}”？\n注：不会检查权限组引用情况，无效权限组会视为“BLOCK”\n(y/n)")
        if (context.nextYorN(timeoutMillis = 15000L) {
                context.print("操作超时，")
                false
        }) {
            context.print("已放弃删除")
            return FATAL
        }
        withLockedAccessing(PmsGroups) {
            PmsGroups[pmsGroup.name] = null
        }
        context.print("已删除权限组“${pmsGroup.name}”")
        return SUCCESS
    }

    /*  ========================  list  ========================  */

    @Method(name = "list", alias = ["l"], pmsLevel = CmdPermissionLevel.OP, title = "列表")
    fun list (): CmdReceipt {
        val table = TableBuilder(4)
        table.th("权限组列表 ——").br()
        for (p in PmsGroups.element.keys()) {
            table.tr(p)
            if (p in PmsGroups.BUILTIN_PMS_GROUPS) table.td("*") else table.td()
            table.td(PmsGroups[p].description.limitEnd(20))
        }
        context.print(table.toString())
        return SUCCESS
    }
}

class PmsGroupNameArgHandler: ArgHandler<String> {
    override val name = "权限组名参数解析器"
    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?): String {
        val input = reader.readString().uppercase()
        if (Regex("[A-Z0-9_]+").matches(input)) return input
        else throw HandlerFatal(input, argName, pos, type)
    }
}

class PmsValueArgHandler: ArgHandler<Int> {
    override val name = "权限值参数解析器"
    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?): Int {
        val input = reader.readString()
        val value = input.toIntOrNull() ?: throw SyntaxError(pos, "无法将“$input”解析为整数值")
        if (value !in -1..1) throw HandlerFatal(input, argName, pos, type)
        return value
    }
}