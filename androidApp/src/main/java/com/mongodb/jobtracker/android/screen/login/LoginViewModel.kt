package com.mongodb.jobtracker.android.screen.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mongodb.jobtracker.RealmRepo
import io.realm.kotlin.mongodb.exceptions.InvalidCredentialsException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {

    private val repo: RealmRepo = RealmRepo()

    private val _loginStatus = MutableLiveData<Boolean>(null)
    val loginStatus: LiveData<Boolean> = _loginStatus

    val alreadyLoggedIn: LiveData<Boolean> = repo.isUserLoggedIn().asLiveData(Dispatchers.Main)

    fun doLogin(userName: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                repo.login(userName, password).apply {
                    withContext(Dispatchers.Main) {
                        _loginStatus.value = true
                    }
                }
            } catch (ex: InvalidCredentialsException) {
                withContext(Dispatchers.Main) {
                    _loginStatus.value = false
                }
            }
        }
    }

}