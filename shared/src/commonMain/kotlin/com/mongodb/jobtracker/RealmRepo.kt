package com.mongodb.jobtracker

import CommonFlow
import asCommonFlow
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.log.LogLevel
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.AppConfiguration
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.User
import io.realm.kotlin.mongodb.sync.SyncConfiguration
import io.realm.kotlin.types.ObjectId
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
                            it.phoneNumber = phoneNumber
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
        println("called")
        val userId = appService.currentUser!!.id


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

            realm.write {
                val location = realm.query<Location>().first().find()!!

                val job = Job().apply {
                    desc = "Random Job"
                    area = findLatest(location)!!.name
                    creationDate = RealmInstant.now()
                    status = Status.UNASSIGNED.name
                    this.user = null
                }
                copyToRealm(job)
            }
        }
    }

    suspend fun getJob(type: Status, location: Location? = null): CommonFlow<List<Job>> {
        val appUser = appService.currentUser ?: return emptyFlow<List<Job>>().asCommonFlow()

        println("context -- ${appUser.id}")
        return withContext(Dispatchers.Default) {
            val currentUser = realm.query<UserInfo>("_id = $0", appUser.id).find().first()

            var realmQuery = when (type) {
                Status.UNASSIGNED -> {
                    realm.query<Job>("status = $0", Status.UNASSIGNED.name)
                }

                Status.DONE -> {
                    realm.query<Job>(
                        "status = $0 && user = $1",
                        Status.DONE.name,
                        currentUser.email
                    )
                }

                Status.ACCEPTED -> {
                    realm.query<Job>(
                        "status = $0 && user = $1",
                        Status.ACCEPTED.name,
                        currentUser.email
                    )
                }
            }

            if (location != null) {
                realmQuery = realmQuery.query("area = $0", location.name)
            }

            realmQuery.asFlow().map { it.list }.asCommonFlow()
        }
    }

    suspend fun updateJobStatus(jobId: ObjectId, currentJobStatus: Status) {

        val appUser = appService.currentUser ?: return

        withContext(Dispatchers.Default) {
            realm.write {
                val currentUser = query<UserInfo>("_id = $0", appUser.id).find().first()
                val currentJob = query<Job>("_id = $0", jobId).find().first()
                copyToRealm(currentJob.apply {
                    this.status = currentJobStatus.name
                    user = currentUser.email
                })
            }
        }
    }

    suspend fun getLocation(): CommonFlow<List<Location>> {
        return withContext(Dispatchers.Default) {
            realm.query<Location>().asFlow().map {
                it.list
            }.asCommonFlow()
        }
    }
}