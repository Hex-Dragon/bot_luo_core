package bot_luo_core.bot

/**
 * # 机器人账号数据类
 *
 * @param id QQ号
 * @param psw QQ密码
 * @param device Mirai使用的设备文件路径
 */
data class BotAccount(
    val id: Long,
    val psw: String,
    val device: String?,
    val working: Boolean?
)
