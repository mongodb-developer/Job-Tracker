package com.mongodb.jobtracker.android.screen.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.mongodb.jobtracker.Job
import com.mongodb.jobtracker.RealmRepo
import com.mongodb.jobtracker.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn

class HomeViewModel : ViewModel() {

    val repo = RealmRepo()
    val unassignedJobs: LiveData<List<Job>> = liveData {
        emitSource(repo.getJob(Status.UNASSIGNED).flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main))
    }
    val doneJobs: LiveData<List<Job>> = liveData {
        emitSource(repo.getJob(Status.DONE).flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main))
    }
    val assignedJobs: LiveData<List<Job>> = liveData {
        emitSource(repo.getJob(Status.ACCEPTED).flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main))
    }

}