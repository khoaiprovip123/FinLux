package com.finlux.app.domain.usecase

import com.finlux.app.core.common.AppResult
import com.finlux.app.domain.model.Category
import com.finlux.app.domain.repository.CategoryRepository
import javax.inject.Inject

class SaveCategoryUseCase @Inject constructor(private val repository: CategoryRepository) {
    suspend operator fun invoke(category: Category): AppResult<String> {
        if (category.name.isBlank()) {
            return AppResult.Error("Vui lòng nhập tên danh mục")
        }
        if (category.isDefault || category.id in com.finlux.app.domain.model.SystemCategories.ALL_SYSTEM_IDS) {
            return AppResult.Error("Không thể chỉnh sửa danh mục hệ thống mặc định")
        }
        return repository.upsertCategory(category)
    }
}
