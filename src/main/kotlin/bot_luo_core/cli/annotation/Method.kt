package bot_luo_core.cli.annotation

import bot_luo_core.cli.Checker
import bot_luo_core.cli.CmdPermissionLevel
import org.apache.logging.log4j.Level
import kotlin.reflect.KClass

annotation class Method(
    /**
     * 同一个命令下[name]相同的方法会视为等效的方法，
     * 以此实现同一命令方法拥有多种参数结构。
     *
     * 这种情况下，有关的[Method]注解需要有相同的[pmsLevel]，[ignoreCheckers]，[usage]，[title]
     * 最好也保证[alias]相同
     */
    val name: String = "",

    /**
     * 别名
     */
    val alias: Array<String> = [],

    /**
     * 忽略的检查器
     *
     * @see[Checker.CHECKERS]
     */
    val ignoreCheckers: Array<KClass<out Checker>> = [],

    /**
     * 追加的检测器
     *
     * @see[Checker.CHECKERS]
     */
    val addonCheckers: Array<KClass<out Checker>> = [],

    /**
     * 默认的权限配置，用于生成默认权限组文件
     */
    val pmsLevel: CmdPermissionLevel = CmdPermissionLevel.DEBUG,

    /**
     * 命令唤起的尝试顺序，数值越大越靠后
     *
     * 若两个命令方法同时满足所有参数和条件，则执行[order]较小者。若[order]相等则无法确定会执行哪一个
     */
    val order: Int = 0,

    val usage: String = "",
    val title: String = "",
    val simples: Array<String> = [],

    /**
     * 默认的日志级别
     *
     * 除发生错误外的日志级别，为[Level]之一
     */
    val defaultLogLevel: String = "INFO"
)
