package com.mongodb.jobtracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform