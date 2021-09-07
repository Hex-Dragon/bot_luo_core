package bot_luo_core.cli.exceptions

enum class CliExceptionLevel {
    /**
     * info是纯粹的返回信息，不会在log中打印堆栈
     */
    INFO,
    WARN,
    ERROR,
    DEBUG
}