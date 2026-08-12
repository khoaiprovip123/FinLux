package com.finlux.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finlux.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class SessionState { CHECKING, AUTHENTICATED, GUEST }

@HiltViewModel
class SplashViewModel @Inject constructor(repository: AuthRepository) : ViewModel() {
    val session = repository.currentUser
        .map { if (it == null) SessionState.GUEST else SessionState.AUTHENTICATED }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState.CHECKING)
}
