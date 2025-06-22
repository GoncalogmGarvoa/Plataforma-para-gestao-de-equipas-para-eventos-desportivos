package pt.arbitros.arbnet.repository.mem

import pt.arbitros.arbnet.domain.universal.Category
import pt.arbitros.arbnet.repository.adaptable_repos.CategoryRepository

class CategoryRepositoryMem: CategoryRepository {
    override fun createCategory(category: Category): Int {
        TODO("Not yet implemented")
    }

    override fun getCategoryNameById(id: Int): String? {
        TODO("Not yet implemented")
    }

    override fun getCategoryIdByName(name: String): Int? {
        TODO("Not yet implemented")
    }

    override fun getAllCategories(): List<Category> {
        TODO("Not yet implemented")
    }

    override fun deleteCategory(id: Int): Boolean {
        TODO("Not yet implemented")
    }
}