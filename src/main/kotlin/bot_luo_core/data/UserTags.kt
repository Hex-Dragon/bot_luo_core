package bot_luo_core.data

import bot_luo_core.cli.CmdContext
import bot_luo_core.util.Tag
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object UserTags: DataObj("data/user_tags.json", 10000L, false)  {
    override fun unload() {}

    val BUILTIN_USER_TAGS = listOf("#this", "#any")

    val tags: HashMap<String, ArrayList<String>> = getObj()

    private fun saveTags() = setObj(tags)

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
            } else if (tags.containsKey(tag)) {
                for (item in tags[tag]!!) {
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
        if (res1.contains("#any")) res.add(Users.virtualUser)
        return res
    }
    fun readUserTag(tag: String) = readUserTag(tag, null)
    fun addToUserTag(tag: String, item: String) = Tag.addToTag(tags, tag, item).apply { saveTags() }
    fun remFromUserTag(tag: String, item: String) = Tag.remFromTag(tags, tag, item).apply { saveTags() }
    fun remUserTag(tag: String) = Tag.remTag(tags, tag).apply { saveTags() }
}