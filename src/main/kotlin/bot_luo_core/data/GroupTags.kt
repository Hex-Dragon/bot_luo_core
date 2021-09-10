package bot_luo_core.data

import bot_luo_core.cli.CmdContext
import bot_luo_core.util.Tag
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object GroupTags: DataObj("data/group_tags.json", 10000L, false) {
    override fun unload() {}

    val BUILTIN_GROUP_TAGS = listOf("#this", "#none")

    val tags: HashMap<String, ArrayList<String>> = getObj()

    private fun saveTags() = setObj(tags)

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
            } else if (tags.containsKey(tag)) {
                for (item in tags[tag]!!) {
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
    fun addToGroupTag(tag: String, item: String) = Tag.addToTag(tags, tag, item).apply { saveTags() }
    fun remFromGroupTag(tag: String, item: String) = Tag.remFromTag(tags, tag, item).apply { saveTags() }
    fun remGroupTag(tag: String) = Tag.remTag(tags, tag).apply { saveTags() }
}