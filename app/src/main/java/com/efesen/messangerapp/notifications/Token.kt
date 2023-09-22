package com.efesen.messangerapp.notifications


/**
 * Created by Efe Şen on 20.09.2023.
 */
class Token {
    private var token: String = ""

    constructor(){}
    constructor(token: String) {
        this.token = token.toString()
    }

    fun getToken(): String? {
        return token
    }

    fun setToken(token: String?) {
        this.token = token!!
    }

}