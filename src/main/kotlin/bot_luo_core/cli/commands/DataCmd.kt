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
import bot_luo_core.data.*
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

    fun get(dataObject: DataObject, objName: String, objId: String = "", path: ArrayList<String> = ArrayList()): CmdReceipt {
        val table = TableBuilder(4)
        table.th("${objName}数据 —— $objId")
        table.p("/"+path.joinToString("/")).br().br()
        try {
            readByPath(dataObject.jsonObj, path, table)
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

    fun get(dataArray: DataArray, arrayName: String, arrayId: String = "", path: ArrayList<String> = ArrayList()): CmdReceipt {
        val table = TableBuilder(4)
        table.th("${arrayName}数据 —— $arrayId")
        table.p("/"+path.joinToString("/")).br().br()
        try {
            readByPath(dataArray.jsonArray, path, table)
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

    @Method(name = "", alias = ["get-group","gg","g"], pmsLevel = CmdPermissionLevel.OP, ignoreCheckers = [GroupCmdWorkingChecker::class], title = "获取群组数据")
    fun getGroup (
        @Argument(name = "群组", handler = GroupArgHandler::class)
        group: Group,
        @Argument(name = "路径", required = false, handler = JsonPathArgHandler::class)
        pathIn: ArrayList<String>?
    ): CmdReceipt = get(group, "群组", "${group.name}(${group.id})", pathIn?: ArrayList())

    @Method(name = "get-user", alias = ["gu","u"], pmsLevel = CmdPermissionLevel.OP, ignoreCheckers = [GroupCmdWorkingChecker::class], title = "获取用户数据")
    fun getUser (
        @Argument(name = "用户", handler = UserArgHandler::class)
        user: User,
        @Argument(name = "路径", required = false, handler = JsonPathArgHandler::class)
        pathIn: ArrayList<String>?
    ): CmdReceipt = get(user, "用户", "${user.name}(${user.id})", pathIn?: ArrayList())

    /*  ========================  set  ========================  */

    suspend fun set(dataObject: DataObject, objName: String, objId: String = "", path: ArrayList<String>, value: String): CmdReceipt {
        val table = TableBuilder(4)
        table.th("写入${objName}数据 —— $objId")
        table.p("/"+path.joinToString("/")).br().br()
        try { withLockedAccessing(dataObject) {
            dataObject.markDirty()
            val (old, new) = writeByPathTyped(dataObject.jsonObj, path, defaultCast(value))
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

    suspend fun set(dataArray: DataArray, arrayName: String, arrayId: String = "", path: ArrayList<String>, value: String): CmdReceipt {
        val table = TableBuilder(4)
        table.th("写入${arrayName}数据 —— $arrayId")
        table.p("/"+path.joinToString("/")).br().br()
        try { withLockedAccessing(dataArray) {
            dataArray.markDirty()
            val (old, new) = writeByPathTyped(dataArray.jsonArray, path, defaultCast(value))
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

    suspend fun set(dataObject: DataObject, objName: String, objId: String = "", path: ArrayList<String>, value: String, type: String): CmdReceipt {
        val table = TableBuilder(4)
        table.th("写入${objName}数据 —— $objId")
        table.p("/"+path.joinToString("/")).br().br()
        try { withLockedAccessing(dataObject) {
            dataObject.markDirty()
            val (old, new) = writeByPathTyped(dataObject.jsonObj, path, value castTo type)
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

    suspend fun set(dataArray: DataArray, arrayName: String, arrayId: String = "", path: ArrayList<String>, value: String, type: String): CmdReceipt {
        val table = TableBuilder(4)
        table.th("写入${arrayName}数据 —— $arrayId")
        table.p("/"+path.joinToString("/")).br().br()
        try { withLockedAccessing(dataArray) {
            dataArray.markDirty()
            val (old, new) = writeByPathTyped(dataArray.jsonArray, path, value castTo type)
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

    @Method(name = "set-group", alias = ["sg"], pmsLevel = CmdPermissionLevel.DEBUG, ignoreCheckers = [GroupCmdWorkingChecker::class], order = 0, title = "修改群组数据")
    suspend fun setGroup (
        @Argument(name = "群组", handler = GroupArgHandler::class)
        group: Group,
        @Argument(name = "路径", handler = JsonPathArgHandler::class)
        path: ArrayList<String>,
        @Argument(name = "值")
        value: String
    ): CmdReceipt = set(group, "群组", "${group.name}(${group.id})", path, value)

    @Method(name = "set-user", alias = ["su"], pmsLevel = CmdPermissionLevel.DEBUG, ignoreCheckers = [GroupCmdWorkingChecker::class], order = 0, title = "修改用户数据")
    suspend fun setUser (
        @Argument(name = "用户", handler = UserArgHandler::class)
        user: User,
        @Argument(name = "路径", handler = JsonPathArgHandler::class)
        path: ArrayList<String>,
        @Argument(name = "值")
        value: String
    ): CmdReceipt = set(user, "用户", "${user.name}(${user.id})", path, value)

    @Method(name = "set-group", alias = ["sg"], pmsLevel = CmdPermissionLevel.DEBUG, ignoreCheckers = [GroupCmdWorkingChecker::class], order = 1, title = "修改群组数据")
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
    ): CmdReceipt  = set(group, "群组", "${group.name}(${group.id})", path, value, type)

    @Method(name = "set-user", alias = ["su"], pmsLevel = CmdPermissionLevel.DEBUG, ignoreCheckers = [GroupCmdWorkingChecker::class], order = 1, title = "修改用户数据")
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
    ): CmdReceipt = set(user, "用户", "${user.name}(${user.id})", path, value, type)

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

        private fun defaultCast(input: String): Any? {
            return input.toBigIntegerOrNull()?:
                input.toBigDecimalOrNull()?:
                input.toBooleanStrictOrNull()?:
                when (input) {
                    "{}" -> JSONObject()
                    "[]" -> JSONArray()
                    "null" -> null
                    else -> input
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
                is Map<*, *> -> "<object: ${value.size}>"
                is List<*> -> "<array: ${value.size}>"
                null -> "null"
                else -> "<${value::class.simpleName}>"
            }
        }

        @Throws(IndexOutOfBoundsException::class, NullPointerException::class)
        private fun readByPath(obj: Any?, path: ArrayList<String> = ArrayList(), table: TableBuilder) {
            if (path.isEmpty()) {
                when (obj) {
                    is JSONObject -> objectGet(obj, table)
                    is JSONArray -> arrayGet(obj, table)
                    null -> throw NullPointerException()
                    else -> table.tr(formatValue(obj))
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