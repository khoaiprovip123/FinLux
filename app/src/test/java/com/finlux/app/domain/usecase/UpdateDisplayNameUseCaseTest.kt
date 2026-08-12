package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.UserProfile
import com.finlux.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class UpdateDisplayNameUseCaseTest {
    private val repository = RecordingAuthRepository()
    private val useCase = UpdateDisplayNameUseCase(repository)

    @Test
    fun `blank name is rejected before repository`() = runTest {
        val result = useCase("   ")

        assertInstanceOf(AppResult.Error::class.java, result)
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun `valid name is trimmed and forwarded`() = runTest {
        val result = useCase("  Nguyễn Thành Long  ")

        assertEquals("Nguyễn Thành Long", repository.lastName)
        assertEquals("Nguyễn Thành Long", (result as AppResult.Success).value.displayName)
    }
}

private class RecordingAuthRepository : AuthRepository {
    var updateCalls = 0
    var lastName = ""
    override val currentUser: Flow<UserProfile?> = flowOf(null)

    override suspend fun updateDisplayName(displayName: String): AppResult<UserProfile> {
        updateCalls++
        lastName = displayName
        return AppResult.Success(UserProfile("uid", displayName, "user@example.com"))
    }

    override suspend fun signIn(email: String, password: String) = unsupported<UserProfile>()
    override suspend fun register(displayName: String, email: String, password: String) = unsupported<UserProfile>()
    override suspend fun sendPasswordReset(email: String) = unsupported<Unit>()
    override suspend fun updateAvatar(jpegBytes: ByteArray) = unsupported<UserProfile>()
    override suspend fun signOut() = Unit

    private fun <T> unsupported(): AppResult<T> = AppResult.Error("Not used")
}
