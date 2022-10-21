package com.mongodb.jobtracker.android.screen.profile

import androidx.lifecycle.*
import com.mongodb.jobtracker.RealmRepo
import com.mongodb.jobtracker.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repo = RealmRepo()

    val userInfo: LiveData<UserInfo?> = liveData {
        emitSource(repo.getUserProfile().flowOn(Dispatchers.IO).asLiveData(Dispatchers.Main))
    }

    fun onLogout() {
        viewModelScope.launch {
            repo.doLogout()
        }
    }

    fun save(name: String, phoneNumber: String) {
        viewModelScope.launch {
            repo.saveUserInfo(name, phoneNumber)
        }
    }

}