package com.mongodb.jobtracker

import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class UserInfo : RealmObject {
    @PrimaryKey
    var _id: String = ""
    var name: String = ""
    var email: String = ""
    var phoneNumber: Long? = null
}

class Job : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.create()
    var status: String? = ""
    var desc: String = ""
    var creationDate: Long? = null
    var area: Location? = null
    var user: UserInfo? = null
}

class Location : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.create()
    var name: String? = ""
}

enum class Status {
    ACCEPTED, DONE, UNASSIGNED
}




