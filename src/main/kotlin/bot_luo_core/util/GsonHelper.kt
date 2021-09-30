package bot_luo_core.util

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject

operator fun JsonObject.set(key: String, value: Any?) {
    this.add(key, Gson().toJsonTree(value))
}

operator fun JsonArray.set(key: Int, value: Any?) {
    this.set(key, Gson().toJsonTree(value))
}