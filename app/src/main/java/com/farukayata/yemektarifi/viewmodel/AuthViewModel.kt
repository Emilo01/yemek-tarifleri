package com.farukayata.yemektarifi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farukayata.yemektarifi.data.remote.AuthRepository
import com.farukayata.yemektarifi.data.remote.UserRepository
import com.farukayata.yemektarifi.data.remote.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.signIn(email, password).fold(
                onSuccess = { onSuccess() },
                onFailure = {  }
            )
        }
    }

    fun signUp(
        name: String,
        surname: String,
        email: String,
        password: String,
        gender: String,
        age: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            authRepository.signUp(email, password).fold(
                onSuccess = { firebaseUser ->
                    val user = User(
                        id = firebaseUser.uid,
                        name = name,
                        surname = surname,
                        email = email,
                        gender = gender,
                        age = age
                    )
                    userRepository.createUser(user).fold(
                        onSuccess = { onSuccess() },
                        onFailure = {  }
                    )
                },
                onFailure = { }
            )
        }
    }
}