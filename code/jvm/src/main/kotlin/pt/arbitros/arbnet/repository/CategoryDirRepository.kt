package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.CategoryDir
import pt.arbitros.arbnet.domain.adaptable.Category

interface CategoryDirRepository {
    fun findCategoryDirEntry(
        refereeId: Int,
        categoryId: Int,
        id: Int,
    ): CategoryDir?

    fun getCategoryDirsByReferee(refereeId: Int): List<CategoryDir>

    fun getCategoryIdByUserId(userId: Int): Int?

    fun updateUserCategory(userId: Int, categoryId: Int): Boolean

    fun removeCategoryDirEntry(
        refereeId: Int,
        categoryId: Int,
        id: Int,
    ): Boolean
}