package com.mongodb.jobtracker

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RealmRepo {

    private val schemaClass = setOf(UserInfo::class, Job::class, Location::class)

    private val appService by lazy {
        val appConfiguration =
            AppConfiguration.Builder(appId = "jobtrackerrealmapp-mlapp").log(LogLevel.ALL).build()
        App.create(appConfiguration)
    }

    private val realm by lazy {
        val user = appService.currentUser!!

        val config =
            SyncConfiguration.Builder(user, schemaClass).name("job-db").schemaVersion(1)
                .initialSubscriptions { realm ->
                    add(realm.query<UserInfo>(), name = "users", updateExisting = true)
                    add(realm.query<Job>(), name = "jobs", updateExisting = true)
                    add(realm.query<Location>(), name = "location", updateExisting = true)
                }.waitForInitialRemoteData().build()
        Realm.open(config)
    }

    suspend fun login(email: String, password: String): User {
        return appService.login(Credentials.emailPassword(email, password))
    }

    suspend fun registration(password: String, email: String) {
        appService.emailPasswordAuth.registerUser(email = email, password = password)
    }

    fun getUserProfile(): Flow<UserInfo?> {


        val userId = appService.currentUser!!.id
        val user = realm.query<UserInfo>("_id = $0", userId).asFlow().map {
            it.list.firstOrNull()
        }
        return user
    }

    fun isUserLoggedIn(): Flow<Boolean> {
        return flowOf(appService.currentUser != null)
    }

    suspend fun saveUserInfo(name: String, phoneNumber: String) {
        withContext(Dispatchers.Default) {
            if (appService.currentUser != null) {
                val userId = appService.currentUser!!.id
                realm.write {
                    var user = query<UserInfo>("_id = $0", userId).first().find()
                    if (user != null) {
                        user = findLatest(user)!!.also {
                            it.name = name
                            it.phoneNumber = phoneNumber.toLongOrNull()
                        }
                        copyToRealm(user)
                    }
                }
            }
        }
    }

    suspend fun doLogout() {
        appService.currentUser?.logOut()
    }

    suspend fun dataSetup() {

        val locations = listOf(
            "New York",
            "Los Angeles",
            "Chicago",
            "Miami",
            "Dallas",
            "Houston",
            "Philadelphia"
        ).map {
            Location().apply { name = it }
        }

        withContext(Dispatchers.Default) {
            realm.write {
                locations.forEach {
                    copyToRealm(it)
                }
            }
        }

        withContext(Dispatchers.Default) {

            val location = realm.query<Location>().first().find()!!

            realm.write {
                val job = Job().apply {
                    desc = "Random Job"
                    area = findLatest(location)
                    creationDate = RealmInstant.now().epochSeconds
                    status = "Unassigned"
                }
                copyToRealm(job)
            }
        }
    }


}