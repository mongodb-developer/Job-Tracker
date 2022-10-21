package com.mongodb.jobtracker.android.screen.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.mongodb.jobtracker.Job
import com.mongodb.jobtracker.RealmRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    val repo = RealmRepo()
    val unassignedJobs : LiveData<List<Job>> = liveData {  }
    val doneJobs : LiveData<List<Job>> = liveData {  }
    val assignedJobs : LiveData<List<Job>> = liveData {  }


    init {
        setupData()
    }

    fun setupData() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.dataSetup()
        }
    }

}