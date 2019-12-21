package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting

object UserHolder {
    private val map = mutableMapOf<String, User>()

    fun registerUser(
        fullName: String,
        email: String,
        password: String
    ): User = User.makeUser(fullName, email = email, password = password)
        .also {
            if (!map.containsKey(it.login))
                map[it.login] = it
            else throw IllegalArgumentException("A user with this email already exists")
        }

    fun registerUserByPhone(fullName: String, rawPhone: String): User {
        if ("^\\+(\\d\\W*){11}$".toRegex().matches(rawPhone)) {
            return User.makeUser(fullName, phone = rawPhone)
                .also {
                    if (!map.containsKey(it.login))
                        map[it.login] = it
                    else throw IllegalArgumentException("A user with this phone already exists")
                }
        } else throw IllegalArgumentException("Enter a valid phone number starting with a + and containing 11 digits")
    }

    fun loginUser(login: String, password: String): String? {
        return (map[login.trim()] ?: map[login.normalizePhoneNumber()])?.run {
            if (checkPassword(password)) this.userInfo
            else null
        }
    }

    fun requestAccessCode(login: String): Unit {
        map[login.normalizePhoneNumber()]?.generateNewAccessCode()
    }

    fun importUsers(list: List<String>): List<User> {
        val result = mutableListOf<User>()
        for (s in list) {
            val (fullName: String, _email: String?, saltHash: String, _phone: String?) = s.split(";")
            val email = if (_email != "") _email else null
            val phone = if (_phone != "") _phone else null
            result.add(User.makeUser(fullName, email, saltHash, phone).also {
                if (!map.containsKey(it.login))
                map[it.login] = it
            })
        }
        return result
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun clearHolder() {
        map.clear()
    }
}

private fun String.normalizePhoneNumber(): String = this.replace("[^+\\d]".toRegex(), "")