package bot_luo_core.cli.commands

import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.checkers.GroupCmdWorkingChecker
import bot_luo_core.cli.exceptions.SyntaxError
import bot_luo_core.cli.handlers.GroupArgHandler
import bot_luo_core.cli.handlers.JsonPathArgHandler
import bot_luo_core.cli.handlers.UserArgHandler
import bot_luo_core.data.Group
import bot_luo_core.data.User
import bot_luo_core.data.withLockedAccessing
import bot_luo_core.util.TableBuilder
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import kotlin.jvm.Throws

@Command(
    name = "data",
    display = "数据",
    alias = [],
    usage = "查看或修改数据对象的值",
    notice = [
        "此命令涉及数据底层，设置值时请谨慎操作"
    ]
)
class DataCmd(context: CmdContext) : Cmd(context) {

    /*  ========================  get  ========================  */

    @Method(name = "", alias = ["get-group","gg","g"], pmsLevel = CmdPermissionLevel.OP, ignoreCheckers = [GroupCmdWorkingChecker::class])
    fun getGroup (
        @Argument(name = "群组", handler = GroupArgHandler::class)
        group: Group,
        @Argument(name = "路径", required = false, handler = JsonPathArgHandler::class)
        pathIn: ArrayList<String>?
    ): CmdReceipt {
        val path = pathIn?: ArrayList()
        val table = TableBuilder(4)
        table.th("群组数据 —— ${group.name}(${group.id})").br()
        table.p("/"+path.joinToString("/")).br()
        try {
            readByPath(group.jsonObj, path, table)
            context.print(table.toString())
            return SUCCESS
        } catch (e: NullPointerException) {
            table.tr("元素不存在")
        } catch (e: IndexOutOfBoundsException) {
            table.tr("数组下标超出范围")
        }
        context.print(table.toString())
        return FATAL
    }

    @Method(name = "get-user", alias = ["gu","u"], pmsLevel = CmdPermissionLevel.OP, ignoreCheckers = [GroupCmdWorkingChecker::class])
    fun getUser (
        @Argument(name = "用户", handler = UserArgHandler::class)
        user: User,
        @Argument(name = "路径", required = false, handler = JsonPathArgHandler::class)
        pathIn: ArrayList<String>?
    ): CmdReceipt {
        val path = pathIn?: ArrayList()
        val table = TableBuilder(4)
        table.th("用户数据 —— ${user.name}(${user.id})").br()
        table.p("/"+path.joinToString("/")).br()
        try {
            readByPath(user.jsonObj, path, table)
            context.print(table.toString())
            return SUCCESS
        } catch (e: NullPointerException) {
            table.tr("元素不存在")
        } catch (e: IndexOutOfBoundsException) {
            table.tr("数组下标超出范围")
        }
        context.print(table.toString())
        return FATAL
    }

    /*  ========================  set  ========================  */

    @Method(name = "set-group", alias = ["sg"], pmsLevel = CmdPermissionLevel.DEBUG, ignoreCheckers = [GroupCmdWorkingChecker::class], order = 0)
    suspend fun setGroup (
        @Argument(name = "群组", handler = GroupArgHandler::class)
        group: Group,
        @Argument(name = "路径", handler = JsonPathArgHandler::class)
        path: ArrayList<String>,
        @Argument(name = "值")
        value: String
    ): CmdReceipt {
        val table = TableBuilder(4)
        table.th("写入群组数据 —— ${group.name}(${group.id})").br()
        table.p("/"+path.joinToString("/")).br()
        try { withLockedAccessing(group) {
            val (old, new) = writeByPath(group.jsonObj, path, value)
            table.tr(formatValue(old)).tb("->").tb(formatValue(new))
            context.print(table.toString())
            return SUCCESS
        } } catch (e: NullPointerException) {
            table.tr("拒绝访问")
        } catch (e: IndexOutOfBoundsException) {
            table.tr("数组下标超出范围")
        }
        context.print(table.toString())
        return FATAL
    }

    @Method(name = "set-user", alias = ["su"], pmsLevel = CmdPermissionLevel.DEBUG, ignoreCheckers = [GroupCmdWorkingChecker::class], order = 0)
    suspend fun setUser (
        @Argument(name = "用户", handler = UserArgHandler::class)
        user: User,
        @Argument(name = "路径", handler = JsonPathArgHandler::class)
        path: ArrayList<String>,
        @Argument(name = "值")
        value: String
    ): CmdReceipt {
        val table = TableBuilder(4)
        table.th("写入用户数据 —— ${user.name}(${user.id})").br()
        table.p("/"+path.joinToString("/")).br()
        try { withLockedAccessing(user) {
            val (old, new) = writeByPath(user.jsonObj, path, value)
            table.tr(formatValue(old)).tb("->").tb(formatValue(new))
            context.print(table.toString())
            return SUCCESS
        } } catch (e: NullPointerException) {
            table.tr("拒绝访问")
        } catch (e: IndexOutOfBoundsException) {
            table.tr("数组下标超出范围")
        }
        context.print(table.toString())
        return FATAL
    }

    @Method(name = "set-group", alias = ["sg"], pmsLevel = CmdPermissionLevel.DEBUG, ignoreCheckers = [GroupCmdWorkingChecker::class], order = 1)
    suspend fun setGroup (
        @Argument(name = "群组", handler = GroupArgHandler::class)
        group: Group,
        @Argument(name = "路径", handler = JsonPathArgHandler::class)
        path: ArrayList<String>,
        @Argument(name = "值")
        value: String,
        @Suppress("UNUSED_PARAMETER")
        @Argument(name = "as", literal = true)
        ignore: Any,
        @Argument(name = "类型")
        type: String
    ): CmdReceipt {
        val table = TableBuilder(4)
        table.th("写入群组数据 —— ${group.name}(${group.id})").br()
        table.p("/"+path.joinToString("/")).br()
        try { withLockedAccessing(group) {
            val (old, new) = writeByPathTyped(group.jsonObj, path, value castTo type)
            table.tr(formatValue(old)).tb("->").tb(formatValue(new))
            context.print(table.toString())
            return SUCCESS
        } } catch (e: NullPointerException) {
            table.tr("拒绝访问")
        } catch (e: IndexOutOfBoundsException) {
            table.tr("数组下标超出范围")
        }
        context.print(table.toString())
        return FATAL
    }

    @Method(name = "set-user", alias = ["su"], pmsLevel = CmdPermissionLevel.DEBUG, ignoreCheckers = [GroupCmdWorkingChecker::class], order = 1)
    suspend fun setUser (
        @Argument(name = "用户", handler = UserArgHandler::class)
        user: User,
        @Argument(name = "路径", handler = JsonPathArgHandler::class)
        path: ArrayList<String>,
        @Argument(name = "值")
        value: String,
        @Suppress("UNUSED_PARAMETER")
        @Argument(name = "as", literal = true)
        ignore: Any,
        @Argument(name = "类型")
        type: String
    ): CmdReceipt {
        val table = TableBuilder(4)
        table.th("写入用户数据 —— ${user.name}(${user.id})").br()
        table.p("/"+path.joinToString("/")).br()
        try { withLockedAccessing(user) {
            val (old, new) = writeByPathTyped(user.jsonObj, path, value castTo type)
            table.tr(formatValue(old)).tb("->").tb(formatValue(new))
            context.print(table.toString())
            return SUCCESS
        } } catch (e: NullPointerException) {
            table.tr("拒绝访问")
        } catch (e: IndexOutOfBoundsException) {
            table.tr("数组下标超出范围")
        }
        context.print(table.toString())
        return FATAL
    }

    @Suppress("UNCHECKED_CAST")
    companion object {

        private infix fun String.castTo(type: String): Any? {
            return when (type.lowercase()) {
                "num","number","float","double" -> this.toBigDecimalOrNull()?: throw SyntaxError(-1, "无法将“$this”转换为浮点数")
                "int","integer","short","long","longlong" -> this.toBigIntegerOrNull()?: throw SyntaxError(-1, "无法将“$this”转换为整数")
                "str","string","charsequence" -> this
                "bool","boolean" -> this.toBoolean()
                "null" -> if (this == "null") return null else throw SyntaxError(-1, "无法将“$this”转换为Null")
                "array","list" -> if (this == "[]") return JSONArray() else throw SyntaxError(-1, "无法将“$this”转换为Array")
                "object","obj" -> if (this == "{}") return JSONObject() else throw SyntaxError(-1, "无法将“$this”转换为Object")
                else -> throw SyntaxError(-1, "未知的类型“$type”")
            }
        }

        private fun objectGet(obj: JSONObject, table: TableBuilder) {
            for ((k, v) in obj.innerMap) {
                table.tr("$k:").tb(formatValue(v))
            }
        }

        private fun arrayGet(array: JSONArray, table: TableBuilder) {
            for (i in array.indices) {
                table.tr("[$i]").tb(formatValue(array[i]))
            }
        }

        private fun formatValue(value: Any?): String {
            return when (value) {
                is Boolean -> value.toString()
                is Number -> value.toString()
                is String -> "\"$value\""
                is Map<*, *> -> "<object>"
                is List<*> -> "<array>"
                null -> "null"
                else -> "<unk>"
            }
        }

        @Throws(IndexOutOfBoundsException::class, NullPointerException::class)
        private fun readByPath(obj: Any?, path: ArrayList<String> = ArrayList(), table: TableBuilder) {
            if (path.isEmpty()) {
                when (obj) {
                    is Boolean -> table.tr(obj)
                    is Number -> table.tr(obj)
                    is String -> table.tr("\"$obj\"")
                    is JSONObject -> objectGet(obj, table)
                    is JSONArray -> arrayGet(obj, table)
                    null -> throw NullPointerException()
                    else -> table.tr("<unk>")
                }
            } else {
                when (obj) {
                    is JSONObject -> {
                        val key = path[0]
                        if (!obj.containsKey(key)) throw NullPointerException()
                        readByPath(obj[key], path.apply { removeAt(0) }, table)
                    }
                    is JSONArray -> {
                        val index = path[0].toIntOrNull() ?: throw NullPointerException()
                        if (index !in obj.indices) throw IndexOutOfBoundsException()
                        readByPath(obj[index], path.apply { removeAt(0) }, table)
                    }
                    else -> throw NullPointerException()
                }
            }
        }

        /**
         * 写入数据
         *
         * 若此路径已存在值，则根据旧值类型转换新值，转换失败则写入[String]
         *
         * 返回 [Pair] (旧数据,新数据)
         */
        @Throws(NullPointerException::class)

        private fun writeByPath(obj: Any?, path: ArrayList<String>, value: String): Pair<Any?,Any> {
            when (path.size) {
                0 -> throw NullPointerException()
                1 -> {
                    when (obj) {
                        is MutableMap<*, *> -> {
                            obj as MutableMap<String, Any?>
                            val oldValue = obj[path[0]]
                            when (obj[path[0]]) {
                                is String -> obj[path[0]] = value
                                is Number -> obj[path[0]] = value.toBigDecimalOrNull()?: value
                                is Boolean -> obj[path[0]] = value.toBooleanStrictOrNull()?: value
                                null -> obj[path[0]] = value
                                else -> throw NullPointerException()
                            }
                            return oldValue to obj[path[0]]!!
                        }
                        is MutableList<*> -> {
                            obj as MutableList<Any>
                            val index = path[0].toIntOrNull()?: throw NullPointerException()
                            if (index !in 0..obj.size) throw IndexOutOfBoundsException()
                            val oldValue: Any?
                            if (index == obj.size) {
                                oldValue = null
                                obj.add(value)
                            } else {
                                oldValue = obj[index]
                                when (obj[index]) {
                                    is String -> obj[index] = value
                                    is Number -> obj[index] = value.toBigDecimalOrNull() ?: value
                                    is Boolean -> obj[index] = value.toBooleanStrictOrNull() ?: value
                                    else -> throw NullPointerException()
                                }
                            }
                            return oldValue to obj[index]
                        }
                        else -> throw NullPointerException()
                    }
                }
                else -> {
                    return when (obj) {
                        is JSONObject -> {
                            val key = path[0]
                            if (!obj.containsKey(key)) throw NullPointerException()
                            writeByPath(obj[key], path.apply { removeAt(0) }, value)
                        }
                        is JSONArray -> {
                            val index = path[0].toIntOrNull() ?: throw NullPointerException()
                            if (index !in obj.indices) throw IndexOutOfBoundsException()
                            writeByPath(obj[index], path.apply { removeAt(0) }, value)
                        }
                        else -> throw NullPointerException()
                    }
                }
            }
        }

        /**
         * 写入数据
         *
         * 不进行类型转换
         *
         * 返回 [Pair] (旧数据,新数据)
         */
        @Throws(NullPointerException::class)
        private fun writeByPathTyped(obj: Any?, path: ArrayList<String>, value: Any?): Pair<Any?,Any?> {
            when (path.size) {
                0 -> throw NullPointerException()
                1 -> {
                    when (obj) {
                        is MutableMap<*, *> -> {
                            obj as MutableMap<String, Any?>
                            val oldValue = obj[path[0]]
                            if (value == null)
                                obj.remove(path[0])
                            else
                                obj[path[0]] = value
                            return oldValue to obj[path[0]]
                        }
                        is MutableList<*> -> {
                            obj as MutableList<Any>
                            val index = path[0].toIntOrNull()?: throw NullPointerException()
                            if (index !in 0..obj.size) throw IndexOutOfBoundsException()
                            val oldValue: Any?
                            if (index == obj.size) {
                                oldValue = null
                                if (value != null)
                                    obj.add(value)
                            } else {
                                oldValue = obj[index]
                                if (value == null)
                                    obj.removeAt(index)
                                else
                                    obj[index] = value
                            }
                            return oldValue to if (value == null) null else obj[index]
                        }
                        else -> throw NullPointerException()
                    }
                }
                else -> {
                    return when (obj) {
                        is JSONObject -> {
                            val key = path[0]
                            if (!obj.containsKey(key)) throw NullPointerException()
                            writeByPathTyped(obj[key], path.apply { removeAt(0) }, value)
                        }
                        is JSONArray -> {
                            val index = path[0].toIntOrNull() ?: throw NullPointerException()
                            if (index !in obj.indices) throw IndexOutOfBoundsException()
                            writeByPathTyped(obj[index], path.apply { removeAt(0) }, value)
                        }
                        else -> throw NullPointerException()
                    }
                }
            }
        }
    }
}