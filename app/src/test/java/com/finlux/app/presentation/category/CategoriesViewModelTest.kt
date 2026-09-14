package com.finlux.app.presentation.category

import app.cash.turbine.test
import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.model.CategoryType
import com.finlux.app.domain.model.SystemCategories
import com.finlux.app.domain.repository.CategoryRepository
import com.finlux.app.domain.usecase.DeleteCategoryUseCase
import com.finlux.app.domain.usecase.SaveCategoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeCategoryRepository
    private lateinit var saveCategoryUseCase: SaveCategoryUseCase
    private lateinit var deleteCategoryUseCase: DeleteCategoryUseCase
    private lateinit var viewModel: CategoriesViewModel

    private val defaultFoodCategory = Category(
        id = SystemCategories.FOOD,
        name = "Ăn uống",
        type = CategoryType.EXPENSE,
        icon = "restaurant",
        colorHex = "#FF5722",
        isDefault = true,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    private val defaultSalaryCategory = Category(
        id = SystemCategories.SALARY,
        name = "Lương",
        type = CategoryType.INCOME,
        icon = "payments",
        colorHex = "#4CAF50",
        isDefault = true,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    private val customGymCategory = Category(
        id = "custom_gym",
        name = "Phòng Gym",
        type = CategoryType.EXPENSE,
        icon = "fitness_center",
        colorHex = "#9C27B0",
        isDefault = false,
        createdAt = Instant.parse("2026-02-01T00:00:00Z"),
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCategoryRepository(
            initialCategories = listOf(defaultFoodCategory, defaultSalaryCategory, customGymCategory)
        )
        saveCategoryUseCase = SaveCategoryUseCase(fakeRepository)
        deleteCategoryUseCase = DeleteCategoryUseCase(fakeRepository)
        viewModel = CategoriesViewModel(
            repository = fakeRepository,
            saveCategory = saveCategoryUseCase,
            deleteCategory = deleteCategoryUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads income and expense categories accurately`() = runTest(testDispatcher) {
        viewModel.categories.test {
            val initial = awaitItem()
            val list = if (initial.isEmpty()) awaitItem() else initial
            assertEquals(3, list.size)
            val expenses = list.filter { it.type == CategoryType.EXPENSE }
            val incomes = list.filter { it.type == CategoryType.INCOME }

            assertEquals(2, expenses.size)
            assertEquals(1, incomes.size)
            assertTrue(expenses.any { it.id == SystemCategories.FOOD })
            assertTrue(expenses.any { it.id == "custom_gym" })
            assertTrue(incomes.any { it.id == SystemCategories.SALARY })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `creates new custom category successfully and invokes callback`() = runTest(testDispatcher) {
        var callbackInvoked = false
        val newCategory = Category(
            id = "",
            name = "Trà sữa & Cà phê",
            type = CategoryType.EXPENSE,
            icon = "coffee",
            colorHex = "#795548",
            isDefault = false,
            createdAt = Instant.now(),
        )

        viewModel.save(newCategory) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
        assertEquals("Đã lưu danh mục", viewModel.actionState.value.message)
        assertFalse(viewModel.actionState.value.isSaving)
        assertTrue(fakeRepository.categories.any { it.name == "Trà sữa & Cà phê" })
    }

    @Test
    fun `edits existing custom category successfully`() = runTest(testDispatcher) {
        var callbackInvoked = false
        val updatedCategory = customGymCategory.copy(name = "Tập Gym & Yoga")

        viewModel.save(updatedCategory) { callbackInvoked = true }
        advanceUntilIdle()

        assertTrue(callbackInvoked)
        assertEquals("Đã lưu danh mục", viewModel.actionState.value.message)
        val stored = fakeRepository.categories.find { it.id == "custom_gym" }
        assertEquals("Tập Gym & Yoga", stored?.name)
    }

    @Test
    fun `deletes custom category successfully`() = runTest(testDispatcher) {
        viewModel.delete(customGymCategory)
        advanceUntilIdle()

        assertEquals("Đã xóa danh mục", viewModel.actionState.value.message)
        assertFalse(fakeRepository.categories.any { it.id == "custom_gym" })
    }

    @Test
    fun `fails to edit system default category and protects domain invariant`() = runTest(testDispatcher) {
        var callbackInvoked = false
        val modifiedFood = defaultFoodCategory.copy(name = "Ăn vặt đổi tên")

        viewModel.save(modifiedFood) { callbackInvoked = true }
        advanceUntilIdle()

        assertFalse(callbackInvoked)
        assertEquals("Không thể chỉnh sửa danh mục hệ thống mặc định", viewModel.actionState.value.message)
        val original = fakeRepository.categories.find { it.id == SystemCategories.FOOD }
        assertEquals("Ăn uống", original?.name)
    }

    @Test
    fun `fails to delete system default category and protects domain invariant`() = runTest(testDispatcher) {
        viewModel.delete(defaultSalaryCategory)
        advanceUntilIdle()

        assertEquals("Không thể xóa danh mục hệ thống mặc định", viewModel.actionState.value.message)
        assertTrue(fakeRepository.categories.any { it.id == SystemCategories.SALARY })
    }

    @Test
    fun `validates category name cannot be blank`() = runTest(testDispatcher) {
        var callbackInvoked = false
        val invalidCategory = Category(
            id = "",
            name = "   ",
            type = CategoryType.EXPENSE,
            icon = "star",
            colorHex = "#000000",
            isDefault = false,
            createdAt = Instant.now(),
        )

        viewModel.save(invalidCategory) { callbackInvoked = true }
        advanceUntilIdle()

        assertFalse(callbackInvoked)
        assertEquals("Vui lòng nhập tên danh mục", viewModel.actionState.value.message)
    }

    @Test
    fun `consumeMessage clears action message`() = runTest(testDispatcher) {
        viewModel.delete(customGymCategory)
        advanceUntilIdle()
        assertEquals("Đã xóa danh mục", viewModel.actionState.value.message)

        viewModel.consumeMessage()
        assertNull(viewModel.actionState.value.message)
    }

    // --- Fake Category Repository ---
    private class FakeCategoryRepository(
        initialCategories: List<Category> = emptyList(),
    ) : CategoryRepository {
        private val _flow = MutableStateFlow(initialCategories)
        val categories: List<Category> get() = _flow.value

        override fun observeCategories(): Flow<List<Category>> = _flow

        override suspend fun upsertCategory(category: Category): AppResult<String> {
            val id = category.id.ifBlank { "cat_${System.currentTimeMillis()}" }
            val stored = category.copy(id = id)
            val current = _flow.value.toMutableList()
            val index = current.indexOfFirst { it.id == id }
            if (index >= 0) {
                current[index] = stored
            } else {
                current.add(stored)
            }
            _flow.value = current
            return AppResult.Success(id)
        }

        override suspend fun deleteCategory(category: Category): AppResult<Unit> {
            _flow.value = _flow.value.filterNot { it.id == category.id }
            return AppResult.Success(Unit)
        }
    }
}
