package com.softylur.roohchat.model

class LastMsg  {
    var uidSender: String? = null
    var uidReceiver: String? = null
    var lastMsg: String? = null
    var lastMsgTime: Long? = null

    constructor(){}

    constructor(uidSender: String?, uidReceiver: String?, lastMsg: String?, lastMsgTime: Long?) {
        this.uidSender = uidSender
        this.uidReceiver = uidReceiver
        this.lastMsg = lastMsg
        this.lastMsgTime = lastMsgTime
    }

}