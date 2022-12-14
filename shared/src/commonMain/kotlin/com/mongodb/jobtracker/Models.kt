package com.mongodb.jobtracker

import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.datetime.Instant

class UserInfo : RealmObject {
    @PrimaryKey
    var _id: String = ""
    var name: String = ""
    var email: String = ""
    var phoneNumber: String = ""
}

class Job : RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId.create()
    var status: String? = ""
    var desc: String = ""
    var creationDate: RealmInstant? = null
    var area: String = ""
    var user: String? = ""
}

fun Job.displayDate(): Long {
    val instant = Instant.fromEpochSeconds(creationDate!!.epochSeconds, creationDate!!.nanosecondsOfSecond)
    return instant.toEpochMilliseconds()
}

class Location : RealmObject {
    @PrimaryKey
    var _id: RealmUUID = RealmUUID.random()
    var name: String = ""
}

enum class Status {
    ACCEPTED, DONE, UNASSIGNED
}




