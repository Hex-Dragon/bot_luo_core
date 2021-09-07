package bot_luo_core.data

object Groups {

    private val groups = HashMap<Long, Group>()
    val activeGroupsCount: Int get() = groups.size
    val virtualGroup: Group get() = readGroup(0)

    fun readGroup(id: Long): Group {
        if (id !in groups.keys) {
            groups[id] = Group(id)
        }
        return groups[id]!!
    }

    fun readGroupOrNull(id: Long): Group? {
        return if (id in groups.keys) {
            groups[id]
        } else {
            val group = Group(id)
            if (group.exists()) {
                groups[id] = group
                group
            } else null
        }
    }

    fun saveAll() {
            groups.values.forEach { it.save() }
    }

    fun remove(group: Group) {
            groups.remove(group.id)
    }
}