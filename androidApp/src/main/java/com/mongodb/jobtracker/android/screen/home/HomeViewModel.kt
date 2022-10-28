package com.mongodb.jobtracker.android.screen.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class HomeViewModel : ViewModel() {

    private val _repo = RealmRepo()
    private val _selectionLocation = MutableStateFlow<Location?>(null)

    val unassignedJobs: LiveData<List<Job>> = _selectionLocation.flatMapLatest {
        _repo.getJob(Status.UNASSIGNED, it)
    }.flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main)

    val doneJobs: LiveData<List<Job>> = _selectionLocation.flatMapLatest {
        _repo.getJob(Status.DONE, it)
    }.flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main)

    val assignedJobs: LiveData<List<Job>> = _selectionLocation.flatMapLatest {
        _repo.getJob(Status.ACCEPTED)
    }.flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main)


    val getLocations: LiveData<List<Location>> = Transformations.map(liveData {
        emitSource(_repo.getLocation().flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main))
    }) {
        it.toMutableList().apply {
            add(0, Location().apply { name = "All Location" })
        }
    }

    fun updateJobStatus(jobId: ObjectId, status: Status) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.updateJobStatus(jobId, status)
        }
    }

    fun onLocationUpdate(location: Location? = null) {
        _selectionLocation.value = location
    }
}