package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import kotlin.reflect.KType

class CmdIdArgHandler: ArgHandler<ArrayList<String>> {

    override val multiValued = true

    override val name = "命令ID解析器"

    override fun handle(
        reader: MessageReader,
        pos: Int,
        argName: String?,
        type: KType?,
        context: CmdContext?
    ): ArrayList<String> {
        val res = ArrayList<String>()
        CmdExArgHandler().handle(reader, pos, argName, type).forEach{
            if (!res.contains(it.id)) res.add(it.id)
        }
        return res  //由于CmdExArgHandler返回结果一定非空，因此不用检查
    }
}