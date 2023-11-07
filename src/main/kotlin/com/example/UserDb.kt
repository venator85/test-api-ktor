package com.example

class UserDb {
    private val users = listOf(
        User("a", "b")
    )

    fun authenticate(username: String, password: String): User? {
        return users.firstOrNull {
            username == it.username && password == it.password
        }
    }

    fun hasUser(username: String): Boolean {
        return users.firstOrNull {
            username == it.username
        } != null
    }
}

data class User(
    val username: String,
    val password: String
)
