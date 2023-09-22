package com.efesen.messangerapp.modelClasses

/**
 * Created by Efe Şen on 18.09.2023.
 */
class ChatList {

    private var id: String =  ""

    constructor()
    constructor(id: String) {
        this.id = id
    }

    fun getId(): String? {
        return id
    }

    fun setId(id:String?) {
        this.id = id!!
    }

}