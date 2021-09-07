package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.data.PmsGroup
import bot_luo_core.data.PmsGroups
import kotlin.reflect.KType

class PmsGroupArgHandler: ArgHandler<PmsGroup> {

    override val name = "权限组参数解析器"

    override fun handle(
        reader: MessageReader,
        pos: Int,
        argName: String?,
        type: KType?,
        context: CmdContext?
    ): PmsGroup {
        val str = reader.readString()
        return PmsGroups.getPmsGroupOrNull(str)?: throw HandlerFatal(str, argName, pos, type)
    }
}