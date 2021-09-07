package bot_luo_core.cli

import java.util.*

enum class CmdPermissionLevel {
    NONE,
    NORMAL,
    HIGH,
    OP,
    DEBUG;

    companion object {
        operator fun invoke(name: String): CmdPermissionLevel {
            return when(name.uppercase(Locale.getDefault())) {
                "NONE" -> NONE
                "NORMAL" -> NORMAL
                "HIGH" -> HIGH
                "OP" -> OP
                "DEBUG" -> DEBUG
                else -> NONE
            }
        }
    }
}