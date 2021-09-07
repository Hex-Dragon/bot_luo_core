package bot_luo_core.data

import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.exceptions.ContextNeeded
import bot_luo_core.util.JsonWorker
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object Tags {
    private const val userFilePath = "data/user_tags.json"
    private const val groupFilePath = "data/group_tags.json"

    val userTag = HashMap<String, ArrayList<String>>()
    val groupTag = HashMap<String, ArrayList<String>>()

    val BUILTIN_GROUP_TAGS = listOf("#this", "#none")
    val BUILTIN_USER_TAGS = listOf("#this")

    init {
        loadTags()
    }

    /**
     * 加载标签文件
     */
    fun loadTags() {
        val ut: HashMap<String,ArrayList<String>> = JsonWorker.readJson(userFilePath)?: HashMap()
        userTag.clear()
        userTag.putAll(ut)

        val gt: HashMap<String,ArrayList<String>> = JsonWorker.readJson(groupFilePath)?: HashMap()
        userTag.clear()
        userTag.putAll(gt)
    }

    /**
     * 保存标签文件
     */
    fun saveTags() {
        JsonWorker.writeJson(userFilePath, userTag)
        JsonWorker.writeJson(groupFilePath, groupTag)
    }

    /**
     * 从标签读取群组列表
     *
     * 标签为'#'开头的字符串，大小写不敏感
     *
     * "#none"指向私聊虚拟群组(0)
     *
     * @return 群组[ArrayList]?，标签无效则返回空列表，缺少环境时返回`null`
     */
    fun readGroupTag(tag: String, context: CmdContext?): ArrayList<Group>? {
        val res1 = ArrayList<String>()
        fun readGroupTag1(tag: String, result: ArrayList<String>) {
            if (tag in BUILTIN_GROUP_TAGS) {
                result.add(tag)
            } else if (groupTag.containsKey(tag)) {
                for (item in groupTag[tag]!!) {
                    if (item !in result)
                        if (item.startsWith('#')) {
                            readGroupTag1(item, result)
                        } else {
                            result.add(item)
                        }
                }
            }
        }
        readGroupTag1(tag.lowercase(Locale.getDefault()).trim(), res1)
        val res = ArrayList<Group>()
        res.addAll(res1.mapNotNull { it.toLongOrNull() }.map { Groups.readGroup(it) })
        if (res1.contains("#this")) {
            if (context == null) return null
            else res.add(context.group)
        }
        if (res1.contains("#none")) res.add(Groups.virtualGroup)
        return res
    }

    fun readGroupTag(tag: String) = readGroupTag(tag,null)

    /**
     * 从标签读取用户列表
     *
     * 标签为'#'开头的字符串，大小写不敏感
     *
     * @return 用户[ArrayList]?，标签无效则返回空列表，缺少环境时返回`null`
     */
    fun readUserTag(tag: String, context: CmdContext?): ArrayList<User>? {
        val res1 = ArrayList<String>()
        fun readUserTag1(tag: String, result: ArrayList<String>) {
            if (tag in BUILTIN_USER_TAGS) {
                result.add(tag)
            } else if (userTag.containsKey(tag)) {
                for (item in userTag[tag]!!) {
                    if (item !in result)
                        if (item.startsWith('#')) {
                            readUserTag1(item, result)
                        } else {
                            result.add(item)
                        }
                }
            }
        }
        readUserTag1(tag.lowercase(Locale.getDefault()).trim(), res1)
        val res = ArrayList<User>()
        res.addAll(res1.mapNotNull { it.toLongOrNull() }.map { Users.readUser(it) })
        if (res1.contains("#this")) {
            if (context == null) return null
            else res.add(context.user)
        }
        return res
    }

    fun readUserTag(tag: String) = readUserTag(tag,null)

    fun addToGroupTag(tag: String, item: String) = addToTag(groupTag, tag, item)

    fun addToUserTag(tag: String, item: String) = addToTag(userTag, tag, item)

    /**
     * ## 向指定标签池的指定标签下添加项
     *
     * 如果项以'#'开头则视为标签嵌套
     *
     * @return 如果出现重复或标签循环嵌套则返回`false`
     */
    fun addToTag(tagPool: HashMap<String, ArrayList<String>>, tag: String, item: String): Boolean {
        if (tagPool.containsKey(tag) && tagPool[tag]!!.contains(item)) return false
        if (item.startsWith('#')) {
            if (item == tag) return false
            val res = ArrayList<String>()
            getParentTags(tagPool, tag, res)
            if (item in res) return false
        }
        if (!tagPool.containsKey(tag)) tagPool[tag] = ArrayList()
        tagPool[tag]!!.add(item)
        return true
    }

    private fun getParentTags(tagPool: HashMap<String, ArrayList<String>>, tag: String, result: ArrayList<String>) {
        for (t in tagPool.keys) {
            if (t in result) continue
            if (tagPool[t]!!.contains(tag)) {
                result.add(t)
                getParentTags(tagPool, t, result)
            }
        }
    }

    fun remFromGroupTag(tag: String, item: String) = remFromTag(groupTag,tag, item)
    fun remFromUserTag(tag: String, item: String) = remFromTag(userTag,tag, item)

    /**
     * ## 从指定标签池的指定标签下移除项
     *
     * @return 如果标签不存在或项不存在于标签下返回`false`
     */
    private fun remFromTag(tagPool: HashMap<String, ArrayList<String>>, tag: String, item: String): Boolean {
        if (tagPool.containsKey(tag) && tagPool[tag]!!.contains(item)) {
            tagPool[tag]!!.remove(item)
            return true
        }
        return false
    }

    fun remGroupTag(tag: String) = remTag(groupTag,tag)
    fun remUserTag(tag: String) = remTag(userTag,tag)

    /**
     * ## 移除整个标签（包括引用）
     *
     * @return 如果标签不存在且标签未被引用返回`false`
     */
    private fun remTag(tagPool: HashMap<String, ArrayList<String>>, tag: String): Boolean {
        var res = false
        if (tagPool.containsKey(tag)) {
            tagPool.remove(tag)
            res = true
        }

        for (i in tagPool.values) {
            if (i.contains(tag)) {
                i.remove(tag)
                res = true
            }
        }
        return res
    }
}