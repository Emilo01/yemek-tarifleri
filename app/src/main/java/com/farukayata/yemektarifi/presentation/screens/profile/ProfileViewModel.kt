package com.farukayata.yemektarifi.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farukayata.yemektarifi.data.remote.model.User
import com.farukayata.yemektarifi.data.remote.AuthRepository
import com.farukayata.yemektarifi.data.remote.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            authRepository.currentUser?.let { firebaseUser ->
                userRepository.getUser(firebaseUser.uid).fold(
                    onSuccess = { user ->
                        _user.value = user
                    },
                    onFailure = {  }
                )
            }
        }
    }

    fun updateProfile(
        name: String,
        surname: String,
        gender: String,
        age: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val currentUser = _user.value ?: return@launch
            val updatedUser = currentUser.copy(
                name = name,
                surname = surname,
                gender = gender,
                age = age
            )
            userRepository.updateUser(updatedUser).fold(
                onSuccess = {
                    _user.value = updatedUser
                    onSuccess()
                },
                onFailure = {  }
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}