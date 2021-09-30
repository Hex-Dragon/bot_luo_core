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
import kotlinx.coroutines.delay
import net.mamoe.mirai.contact.MemberPermission
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
    caption = [
        "可用的公告选项有：",
        "pin         置顶. 可以有多个置顶公告",
        "pop         使用弹窗",
        "edit(e)     显示能够引导群成员修改昵称的窗口",
        "confirm(c)  需要群成员确认",
        "new(n)      发送给新成员"
              ],
    notice = [
        "如果目标群组中无管理bot则无事发生",
        "此命令执行时间通常较长，请耐心等待"
    ]
)
class AnnounceCmd(context: CmdContext) : Cmd(context) {

    @Method(name = "", alias = [], pmsLevel = CmdPermissionLevel.OP, order = 0)
    suspend fun announce (
        @Argument(name = "群组", multiValued = true, handler = MultiGroupArgHandler::class)
        groups: ArrayList<Group>,
        @Argument(name = "公告选项", handler = AnnouncementParametersArgHandler::class)
        builder: AnnouncementParametersBuilder,
        @Argument(name = "头图", handler = ImageArgHandler::class)
        image: Image?,
        @Argument(name = "文本", handler = GreedyStringArgHandler::class)
        content: String
    ): CmdReceipt {

        val t = TableBuilder(4)
        t.th("公告预览 ——").br()

        t.tr("置顶：").td(builder.isPinned)
        t.tr("弹窗：").td(builder.showPopup)
        t.tr("编辑名片：").td(builder.showEditCard)
        t.tr("需要确认：").td(builder.requireConfirmation)
        t.tr("仅新成员：").td(builder.sendToNewMember)
        t.p("头图：").sp()
        if (image != null) t.p(image)
        t.br()
        t.p("文本：").sp().p(content).br()
        t.br().p("确认发送？(y/n)")

        context.sendMessageWithLog(t.toMessage())
        if (!context.nextYorN(timeoutMillis = 30000L) {
                context.print("操作超时，")
                false
        }) {
            context.print("已取消发送")
            return FATAL
        }

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
                table.tr("[√]").td("${group.name}(${group.id})")
                suc ++
                delay((3000L..6000L).random())
            } catch (e: IOException) {
                table.tr("[×]").td("${group.name}(${group.id})").td("网络异常")
            } catch (e: IllegalStateException) {
                table.tr("[×]").td("${group.name}(${group.id})").td("协议异常")
            }

        }.let { if (!it) table.tr("[×]").td("${group.name}(${group.id})").td("无权限") } }
        context.print(table.toString())
        return if (suc > 0) SUCCESS else FATAL
    }

    @Method(name = "", alias = [], pmsLevel = CmdPermissionLevel.OP, order = 1)
    suspend fun announce (
        @Argument(name = "群组", multiValued = true, handler = MultiGroupArgHandler::class)
        groups: ArrayList<Group>,
        @Argument(name = "公告选项", handler = AnnouncementParametersArgHandler::class)
        builder: AnnouncementParametersBuilder,
        @Argument(name = "文本", handler = GreedyStringArgHandler::class)
        content: String
    ): CmdReceipt = announce(groups, builder, null, content)

    @Method(name = "", alias = [], pmsLevel = CmdPermissionLevel.OP, order = 2)
    suspend fun announce (
        @Argument(name = "群组", multiValued = true, handler = MultiGroupArgHandler::class)
        groups: ArrayList<Group>,
        @Argument(name = "头图", handler = ImageArgHandler::class)
        image: Image,
        @Argument(name = "文本", handler = GreedyStringArgHandler::class)
        content: String
    ): CmdReceipt = announce(groups, AnnouncementParametersBuilder(), image, content)

    @Method(name = "", alias = [], pmsLevel = CmdPermissionLevel.OP, order = 3)
    suspend fun announce (
        @Argument(name = "群组", multiValued = true, handler = MultiGroupArgHandler::class)
        groups: ArrayList<Group>,
        @Argument(name = "文本", handler = GreedyStringArgHandler::class)
        content: String
    ): CmdReceipt = announce(groups, AnnouncementParametersBuilder(), null, content)
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
        for (i in input.split(Regex("[,，]"))) {
            when (i) {
                "pin" -> builder.isPinned(true)
                "pop" -> builder.showPopup(true)
                "e", "edit" -> builder.showEditCard(true)
                "c", "confirm" -> builder.requireConfirmation(true)
                "n", "new" -> builder.sendToNewMember(true)
                else -> throw HandlerFatal(i, argName, pos + offset, type)
            }
            offset += i.length
            offset ++
        }
        return builder
    }
}