package com.softylur.roohchat.model

class User {
    var uid: String? = null
    var name: String? = null
    var phoneNumber: String? = null
    var profileImage: String? = null
    var bio: String? = null
    var status: String? = null

    constructor() {}

    constructor(uid: String?, name: String?, phoneNumber: String?, profileImage: String?) {
        this.uid = uid
        this.name = name
        this.phoneNumber = phoneNumber
        this.profileImage = profileImage
    }

    constructor(
        uid: String?,
        name: String?,
        phoneNumber: String?,
        profileImage: String?,
        bio: String?,
        status: String?
    ) {
        this.uid = uid
        this.name = name
        this.phoneNumber = phoneNumber
        this.profileImage = profileImage
        this.bio = bio
        this.status = status
    }

}