package bot_luo_core.util

object Tag {
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

    /**
     * ## 从指定标签池的指定标签下移除项
     *
     * @return 如果标签不存在或项不存在于标签下返回`false`
     */
    fun remFromTag(tagPool: HashMap<String, ArrayList<String>>, tag: String, item: String): Boolean {
        if (tagPool.containsKey(tag) && tagPool[tag]!!.contains(item)) {
            tagPool[tag]!!.remove(item)
            return true
        }
        return false
    }

    /**
     * ## 移除整个标签（包括引用）
     *
     * @return 如果标签不存在且标签未被引用返回`false`
     */
    fun remTag(tagPool: HashMap<String, ArrayList<String>>, tag: String): Boolean {
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

    fun tree(
        tag: String,
        table: TableBuilder,
        line: ArrayList<Int> = ArrayList(),
        tagGetter: (tag: String) -> ArrayList<String>
    ) {
        table.tr()
        line.forEachIndexed { index, l ->
            if (index < line.lastIndex) {
                if (l > 0) table.td("│")
                else table.td("  ")
            } else {
                if (l > 0) table.td("├")
                else table.td("└")
            }
        }
        table.td(tag)
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