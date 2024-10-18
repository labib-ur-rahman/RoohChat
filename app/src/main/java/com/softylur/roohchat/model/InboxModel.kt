package com.softylur.roohchat.model

class InboxModel {
    var uidSender: String? = null
    var uidReceiver: String? = null
    var nameReceiver: String? = null
    var nameSender: String? = null
    var photoUrlReceiver: String? = null
    var photoUrlSender: String? = null
    var lastMsg: String? = null
    var lastMsgTime: String? = null
    var statusReceiver: String? = null
    var statusSender: String? = null

    constructor() {}

    constructor(
        uidSender: String?,
        uidReceiver: String?,
        nameReceiver: String?,
        nameSender: String?,
        photoUrlReceiver: String?,
        photoUrlSender: String?,
        lastMsg: String?,
        lastMsgTime: String?,
        statusReceiver: String?,
        statusSender: String?
    ) {
        this.uidSender = uidSender
        this.uidReceiver = uidReceiver
        this.nameReceiver = nameReceiver
        this.nameSender = nameSender
        this.photoUrlReceiver = photoUrlReceiver
        this.photoUrlSender = photoUrlSender
        this.lastMsg = lastMsg
        this.lastMsgTime = lastMsgTime
        this.statusReceiver = statusReceiver
        this.statusSender = statusSender
    }

}