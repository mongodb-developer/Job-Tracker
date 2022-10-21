package com.mongodb.jobtracker.android.screen.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.mongodb.jobtracker.RealmRepo
import com.mongodb.jobtracker.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repo = RealmRepo()

    val userInfo: LiveData<UserInfo?> = liveData {
        emitSource(repo.getUserProfile()
            .flowOn(Dispatchers.IO)
            .asLiveData(Dispatchers.Main))
    }

    fun onLogout() {
        viewModelScope.launch {
            repo.doLogout()
        }
    }

    fun save(name: String, phoneNumber: String) {
        viewModelScope.launch {
            repo.dataSetup()
            repo.saveUserInfo(name, phoneNumber)
        }
    }

}