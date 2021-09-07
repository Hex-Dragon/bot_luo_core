package bot_luo_core.data

object Users {

    private val users = HashMap<Long, User>()
    val activeUsersCount: Int get() = users.size
    val virtualUser: User get() = readUser(0)

    /**
     * 根据id读取一个[User]，若无记录则自动新建
     */
    fun readUser(id: Long): User {
        if (id !in users.keys) {
            users[id] = User(id)
        }
        return users[id]!!
    }

    /**
     * 根据id读取一个[User]，若无记录则返回`null`
     */
    fun readUserOrNull(id: Long): User? {
        return if (id in users.keys) {
            users[id]
        } else {
            val user = User(id)
            if (user.exists()) {
                users[id] = user
                user
            } else null
        }

    }

    fun saveAll() {
        users.values.forEach { it.save() }
    }

    fun remove(user: User) {
        users.remove(user.id)
    }
}