package pt.arbitros.arbnet.repository.Aux

import pt.arbitros.arbnet.domain.CategoryDir

interface CategoryDirRepository {
    fun assignRefereeToCategoryDir(categoryDir: CategoryDir): Boolean

    fun findCategoryDirEntry(
        refereeId: Int,
        categoryId: Int,
        id: Int,
    ): CategoryDir?

    fun getCategoryDirsByReferee(refereeId: Int): List<CategoryDir>

    fun getCategoryDirsByCategory(categoryId: Int): List<CategoryDir>

    fun updateCategoryDir(categoryDir: CategoryDir): Boolean

    fun removeCategoryDirEntry(
        refereeId: Int,
        categoryId: Int,
        id: Int,
    ): Boolean
}
