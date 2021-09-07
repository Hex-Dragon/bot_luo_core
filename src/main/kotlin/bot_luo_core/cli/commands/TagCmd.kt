package bot_luo_core.cli.commands

import bot_luo_core.cli.Cmd
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.CmdPermissionLevel
import bot_luo_core.cli.CmdReceipt
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.handlers.GroupArgHandler
import bot_luo_core.cli.handlers.TagArgHandler
import bot_luo_core.cli.handlers.UserArgHandler
import bot_luo_core.data.Group
import bot_luo_core.data.Tags
import bot_luo_core.data.Tags.BUILTIN_GROUP_TAGS
import bot_luo_core.data.Tags.BUILTIN_USER_TAGS
import bot_luo_core.data.Tags.addToGroupTag
import bot_luo_core.data.Tags.addToUserTag
import bot_luo_core.data.Tags.groupTag
import bot_luo_core.data.Tags.readGroupTag
import bot_luo_core.data.Tags.readUserTag
import bot_luo_core.data.Tags.remFromGroupTag
import bot_luo_core.data.Tags.remFromUserTag
import bot_luo_core.data.Tags.remGroupTag
import bot_luo_core.data.Tags.remUserTag
import bot_luo_core.data.Tags.userTag
import bot_luo_core.data.User
import bot_luo_core.util.TableBuilder
import bot_luo_core.util.Text.toLowercase
import kotlin.collections.ArrayList

@Command(
    name = "tag",
    display = "标签",
    alias = [],
    usage = "管理用户和群组标签"
)
class TagCmd(context: CmdContext) : Cmd(context) {

    /*  ========================  add  ========================  */

    @Method(name = "group-add", alias = ["ga"], pmsLevel = CmdPermissionLevel.OP, title = "添加群组标签", order = 0)
    fun groupAdd(
        @Argument(display = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(display = "待加入标签", handler = TagArgHandler::class, multiValued = true)
        tagIn: ArrayList<String>
    ): CmdReceipt {
        if (tag in BUILTIN_GROUP_TAGS) {
            context.print("不允许修改内建标签“$tag”")
            return FATAL
        }

        var suc = 0
        val table = TableBuilder(4)
        table.th("群组标签添加 —— 标签($tag)").br()

        for (t in tagIn) {
            if (addToGroupTag(tag, t)) {
                table.tr().tb(t).tb("[√] success added")
                suc++
            } else {
                table.tr().tb(t).tb("[×] duplicated item or circulated tag")
            }
        }
        context.print(table.toString())
        return if (suc > 0) SUCCESS else FATAL
    }

    @Method(name = "group-add", alias = ["ga"], pmsLevel = CmdPermissionLevel.OP, title = "添加群组标签", order = 1)
    fun groupAdd1(
        @Argument(display = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(display = "待加入群组", handler = GroupArgHandler::class, multiValued = true)
        groupIn: ArrayList<Group>
    ): CmdReceipt {
        if (tag in BUILTIN_GROUP_TAGS) {
            context.print("不允许修改内建标签“$tag”")
            return FATAL
        }

        var suc = 0
        val table = TableBuilder(4)
        table.th("群组标签添加 —— 标签($tag)").br()

        for (g in groupIn) {
            if (addToGroupTag(tag, g.id.toString())) {
                table.tr(g.name).tb("(${g.id})").tb("[√] success added")
                suc++
            } else {
                table.tr(g.name).tb("(${g.id})").tb("[×] duplicated item")
            }
        }
        context.print(table.toString())
        return if (suc > 0) SUCCESS else FATAL
    }

    @Method(name = "user-add", alias = ["ua"], pmsLevel = CmdPermissionLevel.OP, title = "添加用户标签", order = 0)
    fun userAdd(
        @Argument(display = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(display = "待加入标签", handler = TagArgHandler::class, multiValued = true)
        tagIn: ArrayList<String>
    ): CmdReceipt {
        if (tag in BUILTIN_USER_TAGS) {
            context.print("不允许修改内建标签“$tag”")
            return FATAL
        }

        var suc = 0
        val table = TableBuilder(4)
        table.th("用户标签添加 —— 标签($tag)").br()

        for (t in tagIn) {
            if (addToUserTag(tag, t)) {
                table.tr().tb(t).tb("[√] success added")
                suc++
            } else {
                table.tr().tb(t).tb("[×] duplicated item or circulated tag")
            }
        }
        context.print(table.toString())
        return if (suc > 0) SUCCESS else FATAL
    }

    @Method(name = "user-add", alias = ["ua"], pmsLevel = CmdPermissionLevel.OP, title = "添加用户标签", order = 1)
    fun userAdd1(
        @Argument(display = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(display = "待加入用户", handler = UserArgHandler::class, multiValued = true)
        userIn: ArrayList<User>
    ): CmdReceipt {
        if (tag in BUILTIN_GROUP_TAGS) {
            context.print("不允许修改内建标签“$tag”")
            return FATAL
        }

        var suc = 0
        val table = TableBuilder(4)
        table.th("用户标签添加 —— 标签($tag)").br()

        for (u in userIn) {
            if (addToUserTag(tag, u.id.toString())) {
                table.tr(u.name).tb("(${u.id})").tb("[√] success added")
                suc++
            } else {
                table.tr(u.name).tb("(${u.id})").tb("[×] duplicated item")
            }
        }
        context.print(table.toString())
        return if (suc > 0) SUCCESS else FATAL
    }

    /*  ========================  list  ========================  */

    @Method(name = "group-list", alias = ["gl"], pmsLevel = CmdPermissionLevel.OP, title = "列出群组标签")
    fun groupList(
        @Argument(display = "标签", handler = TagArgHandler::class, required = false)
        tag: String?
    ): CmdReceipt {
        val table = TableBuilder(4)
        if (tag == null) {
            table.th("群组标签 —— 所有标签").br()
            for (t in groupTag.keys) {
                table.tr(t)
            }
            for (t in BUILTIN_GROUP_TAGS) {
                table.tr(t).tb("*")
            }
            context.print(table.toString())
            return SUCCESS
        } else {
            table.th("群组标签 —— 标签($tag)").br()
            for (g in readGroupTag(tag, context)!!) {
                table.tr("G(${g.id})").tb(g.name)
            }
            context.print(table.toString())
            return SUCCESS
        }
    }

    @Method(name = "user-list", alias = ["ul"], pmsLevel = CmdPermissionLevel.OP, title = "列出用户标签")
    fun userList(
        @Argument(display = "标签", handler = TagArgHandler::class, required = false)
        tag: String?
    ): CmdReceipt {
        val table = TableBuilder(4)
        if (tag == null) {
            table.th("用户标签 —— 所有标签").br()
            for (t in userTag.keys) {
                table.tr(t)
            }
            for (t in BUILTIN_USER_TAGS) {
                table.tr(t).tb("*")
            }
            context.print(table.toString())
            return SUCCESS
        } else {
            table.th("用户标签 —— 标签($tag)").br()
            for (u in readUserTag(tag, context)!!) {
                table.tr("U(${u.id})")
            }
            context.print(table.toString())
            return SUCCESS
        }
    }

    /*  ========================  tree  ========================  */

    @Method(name = "group-tree", alias = ["gt"], pmsLevel = CmdPermissionLevel.OP, title = "展开群组标签")
    fun groupTree(
        @Argument(display = "标签", handler = TagArgHandler::class)
        tag: String
    ): CmdReceipt {
        val table = TableBuilder(1)
        table.th("展开群组标签 —— 标签($tag)")
        tree(tag, table) { groupTag[it] ?: ArrayList() }
        context.print(table.toString())
        return SUCCESS
    }

    @Method(name = "user-tree", alias = ["ut"], pmsLevel = CmdPermissionLevel.OP, title = "展开用户标签")
    fun userTree(
        @Argument(display = "标签", handler = TagArgHandler::class)
        tag: String
    ): CmdReceipt {
        val table = TableBuilder(1)
        table.th("展开用户标签 —— 标签($tag)")
        tree(tag, table) { userTag[it] ?: ArrayList() }
        context.print(table.toString())
        return SUCCESS
    }

    /*  ========================  remove  ========================  */

    @Method(name = "group-remove", alias = ["gr"], pmsLevel = CmdPermissionLevel.OP, title = "删除群组标签", order = 0)
    suspend fun groupRem(
        @Argument(display = "待删除标签", handler = TagArgHandler::class)
        tag: String
    ): CmdReceipt {
        if (tag in BUILTIN_GROUP_TAGS) {
            context.print("不允许修改内建标签")
            return FATAL
        }
        context.sendMessageWithLog(context.user.getAt() + "是否要删除整个 $tag 标签及其引用（y/n）")
        return if (context.nextYorN(timeoutMillis = 15000) {
            context.print("操作超时，")
            false
        }) {
            if (remGroupTag(tag)) {
                context.print("已移除标签 $tag 及其引用")
                SUCCESS
            } else {
                context.print("标签 $tag 不存在且未被引用")
                FATAL
            }
        } else {
            context.print("已放弃操作")
            FATAL
        }
    }

    @Method(name = "user-remove", alias = ["ur"], pmsLevel = CmdPermissionLevel.OP, title = "删除用户标签", order = 0)
    suspend fun userRem(
        @Argument(display = "待删除标签", handler = TagArgHandler::class)
        tag: String
    ): CmdReceipt {
        if (tag in BUILTIN_USER_TAGS) {
            context.print("不允许修改内建标签")
            return FATAL
        }
        context.sendMessageWithLog(context.user.getAt() + "是否要删除整个 $tag 标签及其引用（y/n）")
        return if (context.nextYorN(timeoutMillis = 15000) {
                context.print("操作超时，")
                false
            }) {
            if (remUserTag(tag)) {
                context.print("已移除标签 $tag 及其引用")
                SUCCESS
            } else {
                context.print("标签 $tag 不存在且未被引用")
                FATAL
            }
        } else {
            context.print("已放弃操作")
            FATAL
        }
    }

    @Method(name = "group-remove", alias = ["gr"], pmsLevel = CmdPermissionLevel.OP, title = "移除群组标签", order = 1)
    fun groupRem(
        @Argument(display = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(display = "待移除群组或标签", multiValued = true)
        itemsIn: ArrayList<String>
    ): CmdReceipt {
        if (tag in BUILTIN_GROUP_TAGS) {
            context.print("不允许修改内建标签")
            return FATAL
        }
        val table = TableBuilder(4)
        table.th("移除群组标签 —— 标签($tag)")
        for (i in itemsIn) {
            if (remFromGroupTag(tag, i.toLowercase()))
                table.tr().tb(i).tb("[√] success removed")
            else
                table.tr().tb(i).tb("[×] tag or item not found")
        }
        context.print(table.toString())
        return SUCCESS
    }

    @Method(name = "group-remove", alias = ["gr"], pmsLevel = CmdPermissionLevel.OP, title = "移除群组标签", order = 1)
    fun userRem(
        @Argument(display = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(display = "待移除用户或标签", multiValued = true)
        itemsIn: ArrayList<String>
    ): CmdReceipt {
        if (tag in BUILTIN_GROUP_TAGS) {
            context.print("不允许修改内建标签")
            return FATAL
        }
        val table = TableBuilder(4)
        table.th("移除用户标签 —— 标签($tag)")
        for (i in itemsIn) {
            if (remFromUserTag(tag, i.toLowercase()))
                table.tr().tb(i).tb("[√] success removed")
            else
                table.tr().tb(i).tb("[×] tag or item not found")
        }
        context.print(table.toString())
        return SUCCESS
    }

    override fun onExit(receipt: CmdReceipt) {
        if (receipt.needSave()) Tags.saveTags()
    }

    companion object {
        private fun tree(
            tag: String,
            table: TableBuilder,
            line: ArrayList<Int> = ArrayList(),
            tagGetter: (tag: String) -> ArrayList<String>
        ) {
            table.tr()
            line.forEachIndexed { index, l ->
                if (index < line.lastIndex) {
                    if (l > 0) table.tb("│")
                    else table.tb("  ")
                } else {
                    if (l > 0) table.tb("├")
                    else table.tb("└")
                }
            }
            table.tb(tag)
            val items = tagGetter(tag)
            items.sortedWith { o1, o2 ->
                var r = 0
                if (o1.startsWith('#')) r++
                if (o2.startsWith('#')) r--
                r
            }
            items.forEachIndexed { index, i ->
                line.add(items.lastIndex - index)

                tree(i, table, line, tagGetter)

                line.removeLast()
            }
        }
    }
}