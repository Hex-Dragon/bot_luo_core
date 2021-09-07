package bot_luo_core.cli.handlers

import bot_luo_core.cli.ArgHandler
import bot_luo_core.cli.CmdContext
import bot_luo_core.cli.MessageReader
import bot_luo_core.cli.exceptions.HandlerFatal
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.content
import kotlin.reflect.KType

class ImageArgHandler: ArgHandler<Image> {
    override val name: String = "图片参数解析器"

    override fun handle(reader: MessageReader, pos: Int, argName: String?, type: KType?, context: CmdContext?): Image {
        return reader.readImage()?: throw HandlerFatal(reader.readMessage().content, argName, pos, type)
    }
}