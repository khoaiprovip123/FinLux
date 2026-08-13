package com.finlux.app.presentation.auth

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val repository: AuthRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: AuthViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `signIn with invalid email emits error state`() = runTest {
        viewModel.updateEmail("invalid-email")
        viewModel.updatePassword("12345678")

        viewModel.signIn()

        val currentState = viewModel.state.value
        assertEquals("Email không hợp lệ", currentState.error)
        assertFalse(currentState.isLoading)
    }

    @Test
    fun `signIn with valid credentials updates state from loading to completed`() = runTest {
        val userProfile = UserProfile("uid1", "Test User", "test@finlux.app")
        coEvery { repository.signIn("test@finlux.app", "Password123") } returns AppResult.Success(userProfile)

        viewModel.updateEmail("test@finlux.app")
        viewModel.updatePassword("Password123")

        viewModel.state.test {
            val initial = awaitItem()
            assertFalse(initial.isLoading)
            assertFalse(initial.completed)

            viewModel.signIn()

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertNull(loadingState.error)

            val completedState = awaitItem()
            assertFalse(completedState.isLoading)
            assertTrue(completedState.completed)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signIn when repository fails emits error message`() = runTest {
        coEvery { repository.signIn("test@finlux.app", "WrongPassword") } returns AppResult.Error("Sai mật khẩu")

        viewModel.updateEmail("test@finlux.app")
        viewModel.updatePassword("WrongPassword")

        viewModel.state.test {
            awaitItem() // Initial

            viewModel.signIn()

            awaitItem() // Loading

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertFalse(errorState.completed)
            assertEquals("Sai mật khẩu", errorState.error)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `register with mismatched confirm password emits validation error`() = runTest {
        viewModel.updateDisplayName("Thành Long")
        viewModel.updateEmail("user@finlux.app")
        viewModel.updatePassword("Password123")
        viewModel.updateConfirmPassword("Password999")

        viewModel.register()

        val currentState = viewModel.state.value
        assertEquals("Mật khẩu xác nhận không khớp", currentState.error)
        assertFalse(currentState.isLoading)
    }
}
