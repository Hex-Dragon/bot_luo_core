package bot_luo_core.cli.annotation

annotation class Command(
    val name: String,
    val display: String,
    val alias: Array<String> = [],
    val usage: String = "",
    val notice: Array<String> = [],
    val caption: Array<String> = []
)
