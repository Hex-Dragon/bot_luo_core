package bot_luo_core.cli

enum class CmdExitState(val addCount: Boolean, val setTime: Boolean) {
    SUCCESS(true, true),
    FATAL(false, false),
    FATAL_BUT_SET_TIME(false, true),
    FATAL_BUT_ADD_COUNT(true, false)
}
