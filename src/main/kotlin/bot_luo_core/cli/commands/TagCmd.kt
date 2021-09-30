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
import bot_luo_core.data.*
import bot_luo_core.data.GroupTags.BUILTIN_GROUP_TAGS
import bot_luo_core.data.GroupTags.addToGroupTag
import bot_luo_core.data.GroupTags.readGroupTag
import bot_luo_core.data.GroupTags.remFromGroupTag
import bot_luo_core.data.GroupTags.remGroupTag
import bot_luo_core.data.UserTags.BUILTIN_USER_TAGS
import bot_luo_core.data.UserTags.addToUserTag
import bot_luo_core.data.UserTags.readUserTag
import bot_luo_core.data.UserTags.remFromUserTag
import bot_luo_core.data.UserTags.remUserTag
import bot_luo_core.util.TableBuilder
import bot_luo_core.util.Tag.tree
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
    suspend fun groupAdd(
        @Argument(name = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(name = "待加入标签", handler = TagArgHandler::class, multiValued = true)
        tagIn: ArrayList<String>
    ): CmdReceipt {
        if (tag in BUILTIN_GROUP_TAGS) {
            context.print("不允许修改内建标签“$tag”")
            return FATAL
        }

        var suc = 0
        val table = TableBuilder(4)
        table.th("群组标签添加 —— 标签($tag)").br()

        withLockedAccessing(GroupTags) {
            for (t in tagIn) {
                if (addToGroupTag(tag, t)) {
                    table.tr().td(t).td("[√] success added")
                    suc++
                } else {
                    table.tr().td(t).td("[×] duplicated item or circulated tag")
                }
            }
        }
        context.print(table.toString())
        return if (suc > 0) SUCCESS else FATAL
    }

    @Method(name = "group-add", alias = ["ga"], pmsLevel = CmdPermissionLevel.OP, title = "添加群组标签", order = 1)
    suspend fun groupAdd1(
        @Argument(name = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(name = "待加入群组", handler = GroupArgHandler::class, multiValued = true)
        groupIn: ArrayList<Group>
    ): CmdReceipt {
        if (tag in BUILTIN_GROUP_TAGS) {
            context.print("不允许修改内建标签“$tag”")
            return FATAL
        }

        var suc = 0
        val table = TableBuilder(4)
        table.th("群组标签添加 —— 标签($tag)").br()

        withLockedAccessing(GroupTags) {
            for (g in groupIn) {
                if (addToGroupTag(tag, g.id.toString())) {
                    table.tr(g.name).td("(${g.id})").td("[√] success added")
                    suc++
                } else {
                    table.tr(g.name).td("(${g.id})").td("[×] duplicated item")
                }
            }
        }
        context.print(table.toString())
        return if (suc > 0) SUCCESS else FATAL
    }

    @Method(name = "user-add", alias = ["ua"], pmsLevel = CmdPermissionLevel.OP, title = "添加用户标签", order = 0)
    suspend fun userAdd(
        @Argument(name = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(name = "待加入标签", handler = TagArgHandler::class, multiValued = true)
        tagIn: ArrayList<String>
    ): CmdReceipt {
        if (tag in BUILTIN_USER_TAGS) {
            context.print("不允许修改内建标签“$tag”")
            return FATAL
        }

        var suc = 0
        val table = TableBuilder(4)
        table.th("用户标签添加 —— 标签($tag)").br()

        withLockedAccessing(UserTags) {
            for (t in tagIn) {
                if (addToUserTag(tag, t)) {
                    table.tr().td(t).td("[√] success added")
                    suc++
                } else {
                    table.tr().td(t).td("[×] duplicated item or circulated tag")
                }
            }
        }
        context.print(table.toString())
        return if (suc > 0) SUCCESS else FATAL
    }

    @Method(name = "user-add", alias = ["ua"], pmsLevel = CmdPermissionLevel.OP, title = "添加用户标签", order = 1)
    suspend fun userAdd1(
        @Argument(name = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(name = "待加入用户", handler = UserArgHandler::class, multiValued = true)
        userIn: ArrayList<User>
    ): CmdReceipt {
        if (tag in BUILTIN_GROUP_TAGS) {
            context.print("不允许修改内建标签“$tag”")
            return FATAL
        }

        var suc = 0
        val table = TableBuilder(4)
        table.th("用户标签添加 —— 标签($tag)").br()

        withLockedAccessing(UserTags) {
            for (u in userIn) {
                if (addToUserTag(tag, u.id.toString())) {
                    table.tr(u.name).td("(${u.id})").td("[√] success added")
                    suc++
                } else {
                    table.tr(u.name).td("(${u.id})").td("[×] duplicated item")
                }
            }
        }
        context.print(table.toString())
        return if (suc > 0) SUCCESS else FATAL
    }

    /*  ========================  list  ========================  */

    @Method(name = "group-list", alias = ["gl"], pmsLevel = CmdPermissionLevel.OP, title = "列出群组标签")
    fun groupList(
        @Argument(name = "标签", handler = TagArgHandler::class, required = false)
        tag: String?
    ): CmdReceipt {
        val table = TableBuilder(4)
        if (tag == null) {
            table.th("群组标签 —— 所有标签").br()
            for (t in GroupTags.tags.keys) {
                table.tr(t)
            }
            for (t in BUILTIN_GROUP_TAGS) {
                table.tr(t).td("*")
            }
            context.print(table.toString())
            return SUCCESS
        } else {
            table.th("群组标签 —— 标签($tag)").br()
            for (g in readGroupTag(tag, context)!!) {
                table.tr("G(${g.id})").td(g.name)
            }
            context.print(table.toString())
            return SUCCESS
        }
    }

    @Method(name = "user-list", alias = ["ul"], pmsLevel = CmdPermissionLevel.OP, title = "列出用户标签")
    fun userList(
        @Argument(name = "标签", handler = TagArgHandler::class, required = false)
        tag: String?
    ): CmdReceipt {
        val table = TableBuilder(4)
        if (tag == null) {
            table.th("用户标签 —— 所有标签").br()
            for (t in UserTags.tags.keys) {
                table.tr(t)
            }
            for (t in BUILTIN_USER_TAGS) {
                table.tr(t).td("*")
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
        @Argument(name = "标签", handler = TagArgHandler::class)
        tag: String
    ): CmdReceipt {
        val table = TableBuilder(1)
        table.th("展开群组标签 —— 标签($tag)")
        tree(tag, table) { GroupTags.tags[it] ?: ArrayList() }
        context.print(table.toString())
        return SUCCESS
    }

    @Method(name = "user-tree", alias = ["ut"], pmsLevel = CmdPermissionLevel.OP, title = "展开用户标签")
    fun userTree(
        @Argument(name = "标签", handler = TagArgHandler::class)
        tag: String
    ): CmdReceipt {
        val table = TableBuilder(1)
        table.th("展开用户标签 —— 标签($tag)")
        tree(tag, table) { UserTags.tags[it] ?: ArrayList() }
        context.print(table.toString())
        return SUCCESS
    }

    /*  ========================  remove  ========================  */

    @Method(name = "group-remove", alias = ["gr"], pmsLevel = CmdPermissionLevel.OP, title = "删除群组标签", order = 0)
    suspend fun groupRem(
        @Argument(name = "待删除标签", handler = TagArgHandler::class)
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
            if (withLockedAccessing(GroupTags) { remGroupTag(tag) }) {
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
        @Argument(name = "待删除标签", handler = TagArgHandler::class)
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
            if (withLockedAccessing(UserTags) { remUserTag(tag) }) {
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
    suspend fun groupRem(
        @Argument(name = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(name = "待移除群组或标签", multiValued = true)
        itemsIn: ArrayList<String>
    ): CmdReceipt {
        if (tag in BUILTIN_GROUP_TAGS) {
            context.print("不允许修改内建标签")
            return FATAL
        }
        val table = TableBuilder(4)
        table.th("移除群组标签 —— 标签($tag)")
        withLockedAccessing(GroupTags) {
            for (i in itemsIn) {
                if (remFromGroupTag(tag, i.toLowercase()))
                    table.tr().td(i).td("[√] success removed")
                else
                    table.tr().td(i).td("[×] tag or item not found")
            }
        }
        context.print(table.toString())
        return SUCCESS
    }

    @Method(name = "group-remove", alias = ["gr"], pmsLevel = CmdPermissionLevel.OP, title = "移除群组标签", order = 1)
    suspend fun userRem(
        @Argument(name = "被操作标签", handler = TagArgHandler::class)
        tag: String,
        @Argument(name = "待移除用户或标签", multiValued = true)
        itemsIn: ArrayList<String>
    ): CmdReceipt {
        if (tag in BUILTIN_GROUP_TAGS) {
            context.print("不允许修改内建标签")
            return FATAL
        }
        val table = TableBuilder(4)
        table.th("移除用户标签 —— 标签($tag)")
        withLockedAccessing(UserTags) {
            for (i in itemsIn) {
                if (remFromUserTag(tag, i.toLowercase()))
                    table.tr().td(i).td("[√] success removed")
                else
                    table.tr().td(i).td("[×] tag or item not found")
            }
        }
        context.print(table.toString())
        return SUCCESS
    }
}