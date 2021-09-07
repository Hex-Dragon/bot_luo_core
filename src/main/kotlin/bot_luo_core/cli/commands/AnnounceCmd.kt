package bot_luo_core.cli.commands

import bot_luo_core.cli.*
import bot_luo_core.cli.annotation.Argument
import bot_luo_core.cli.annotation.Command
import bot_luo_core.cli.annotation.Method
import bot_luo_core.cli.exceptions.HandlerFatal
import bot_luo_core.cli.handlers.GreedyStringArgHandler
import bot_luo_core.cli.handlers.ImageArgHandler
import bot_luo_core.cli.handlers.MultiGroupArgHandler
import bot_luo_core.data.Group
import bot_luo_core.util.TableBuilder
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.announcement.Announcement
import net.mamoe.mirai.contact.announcement.Announcement.Companion.publishAnnouncement
import net.mamoe.mirai.contact.announcement.AnnouncementParametersBuilder
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.IOException
import java.lang.IllegalStateException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.reflect.KType

@Command(
    name = "announce",
    display = "公告",
    alias = ["anc"],
    usage = "发送群公告",
    notice = [
        "如果目标群组中无管理bot则无事发生",
        "此命令执行时间通常较长，请耐心等待"
    ]
)
class AnnounceCmd(context: CmdContext) : Cmd(context) {

    @Method(name = "", alias = [], pmsLevel = CmdPermissionLevel.OP)
    suspend fun announce (
        @Argument(display = "群组", multiValued = true, handler = MultiGroupArgHandler::class)
        groups: ArrayList<Group>,
        @Argument(display = "公告选项", handler = AnnouncementParametersArgHandler::class)
        builder: AnnouncementParametersBuilder,
        @Argument(display = "头图", handler = ImageArgHandler::class)
        image: Image?,
        @Argument(display = "文本", handler = GreedyStringArgHandler::class)
        content: String
    ): CmdReceipt {
        var suc = 0
        val table = TableBuilder(4)
        table.th("发送公告 —— ").br()
        groups.forEach { group -> group.withServeBot(MemberPermission.ADMINISTRATOR) { contact ->
            try {
                if (image != null) {
                    val connection = URL(image.queryUrl()).openConnection() as HttpURLConnection
                    connection.connect()
                    connection.inputStream.use { builder.image = contact.announcements.uploadImage(it.toExternalResource()) }
                    connection.disconnect()
                }
                contact.publishAnnouncement(content, builder.build())
                table.tr("[√]").tb("${group.name}(${group.id})")
                suc ++
            } catch (e: IOException) {
                table.tr("[×]").tb("${group.name}(${group.id})").tb("网络异常")
            } catch (e: IllegalStateException) {
                table.tr("[×]").tb("${group.name}(${group.id})").tb("协议异常")
            }

        }.let { if (!it) table.tr("[×]").tb("${group.name}(${group.id})").tb("无权限") } }
        context.print(table.toString())
        return if (suc > 0) SUCCESS else FATAL
    }
}

class AnnouncementParametersArgHandler: ArgHandler<AnnouncementParametersBuilder> {

    override val name = "公告选项参数解析器"

    override fun handle(
        reader: MessageReader,
        pos: Int,
        argName: String?,
        type: KType?,
        context: CmdContext?
    ): AnnouncementParametersBuilder {
        val input = reader.readString()
        val builder = AnnouncementParametersBuilder()
        var offset = 0
        for (i in input.split(Regex(",，"))) {
            when (i) {
                "pin" -> builder.isPinned(true)
                "pop" -> builder.showPopup(true)
                "e", "edit" -> builder.showEditCard(true)
                "c", "confirm" -> builder.requireConfirmation(true)
                else -> throw HandlerFatal(i, argName, pos + offset, type)
            }
            offset += i.length
            offset ++
        }
        return builder
    }
}