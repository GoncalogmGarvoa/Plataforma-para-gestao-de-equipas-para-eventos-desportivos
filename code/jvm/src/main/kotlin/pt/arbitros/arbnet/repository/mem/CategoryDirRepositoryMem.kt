package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.CategoryDir
import pt.arbitros.arbnet.repository.CategoryDirRepository

class CategoryDirRepositoryMem: CategoryDirRepository {
    override fun findCategoryDirEntry(
        refereeId: Int,
        categoryId: Int,
        id: Int
    ): CategoryDir? {
        TODO("Not yet implemented")
    }

    override fun getCategoryDirsByReferee(refereeId: Int): List<CategoryDir> {
        TODO("Not yet implemented")
    }

    override fun getCategoryIdByUserId(userId: Int): Int? {
        TODO("Not yet implemented")
    }

    override fun updateUserCategory(userId: Int, categoryId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeCategoryDirEntry(refereeId: Int, categoryId: Int, id: Int): Boolean {
        TODO("Not yet implemented")
    }
}