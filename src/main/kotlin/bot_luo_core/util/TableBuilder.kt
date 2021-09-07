package bot_luo_core.util

import bot_luo_core.util.Text.times
import net.mamoe.mirai.message.data.*

class TableBuilder (
    /**
     * 单元间分隔符
     */
    private val separator: String
        ) {

    constructor(space: Int) : this(' ' * space)

    private val mcb = MessageChainBuilder()
    private val table = ArrayList<ArrayList<String>>()
    private val ps = HashMap<Int, MessageChain>()
    private val len = ArrayList<Int>()
    private var x = -1
    private var y = -1

    /**
     * 插入一个表头，若输入不为空则自带一个换行
     *
     * 完成上一个表的排版，新开一个表
     */
    fun th(msg: Message? = null): TableBuilder {
        build()
        if (msg != null) {
            mcb.append(msg)
            mcb.append('\n')
        }
        return this
    }
    fun th(str: String) = th(str.toPlainText())

    /**
     * 新开一行
     *
     * @param str 文本，为`null`则不添加单元
     */
    fun tr(str: String? = null): TableBuilder {
        x = 0
        table.add(ArrayList())
        y++
        if (str != null) tb(str)
        return this
    }
    fun tr(any: Any?): TableBuilder = tr(any?.toString())

    /**
     * 附加一个单元
     *
     * 必须在[tr]之后使用
     *
     * @param str 文本，默认为""
     */
    fun tb(str: String = ""): TableBuilder {
        table[y].add(str)
        while (len.size <= x) len.add(0)
        if (len[x] < str.length) len[x] = str.length
        x++
        return this
    }
    fun tb(any: Any?) = tb(any?.toString()?:"")

    /**
     * 附加到独立的一行，不影响当前表格布局
     */
    fun p(msg: Message): TableBuilder {
        if (y == -1) mcb.append(msg)
        else {
            if (ps[y] == null) ps[y] = EmptyMessageChain
            ps[y] = ps[y]!!.plus(msg)
        }
        return this
    }
    fun p(str: String) = p(str.toPlainText())

    /**
     * 插入一个间隔到独立行
     */
    fun sp(): TableBuilder {
        p(separator)
        return this
    }

    /**
     * 独立行换行，不影响表格布局
     */
    fun br(): TableBuilder {
        if (y == -1) mcb.append('\n')
        else {
            if (ps[y] == null) ps[y] = EmptyMessageChain
            ps[y] = ps[y]!!.plus("\n")
        }
        return this
    }

    private fun build() {
        for (i in table.indices) {
            for (j in table[i].indices) {
                mcb.append(table[i][j]).append("  " * (len[j]-table[i][j].length))
                if (j < table[i].lastIndex) mcb.append(separator)
            }
            mcb.append('\n')
            if (ps[i] != null) mcb.append(ps[i]!!)
        }
        table.clear()
        len.clear()
        ps.clear()
        //操作开始前x,y总是指向下一个空位
        x = -1
        y = -1
    }

    fun prettyLines(title: String, lines: Int, action: (index: Int, builder: TableBuilder)->Unit): TableBuilder {
        when (lines) {
            0 -> {}
            1 -> {
                tr(title)
                action(0, this)
            }
            else -> {
                tr(title)
                for (i in 0 until lines) {
                    tr("")
                    action(i, this)
                }
            }
        }
        return this
    }

    override fun toString(): String {
        build()
        return mcb.build().content.trim()
    }

    fun toMessage(): MessageChain {
        build()
        return mcb.build()
    }

}