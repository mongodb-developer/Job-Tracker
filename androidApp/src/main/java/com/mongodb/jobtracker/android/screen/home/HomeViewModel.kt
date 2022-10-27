package com.mongodb.jobtracker.android.screen.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.mongodb.jobtracker.Job
import com.mongodb.jobtracker.Location
import com.mongodb.jobtracker.RealmRepo
import com.mongodb.jobtracker.Status
import io.realm.kotlin.types.ObjectId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    val repo = RealmRepo()
    val unassignedJobs: LiveData<List<Job>> = liveData {
        emitSource(
            repo.getJob(Status.UNASSIGNED).flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main)
        )
    }
    val doneJobs: LiveData<List<Job>> = liveData {
        emitSource(repo.getJob(Status.DONE).flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main))
    }
    val assignedJobs: LiveData<List<Job>> = liveData {
        emitSource(repo.getJob(Status.ACCEPTED).flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main))
    }
    val getLocations: LiveData<List<Location>> =
        liveData {
            emitSource(repo.getLocation().flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main))
        }

    fun updateJobStatus(jobId: ObjectId) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateJobStatus(jobId)
        }
    }
}