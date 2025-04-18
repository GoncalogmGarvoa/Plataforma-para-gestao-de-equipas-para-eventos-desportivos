package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.Category

interface CategoryRepository {

    fun createCategory(category: Category): Int

    fun findCategoryById(id: Int): Category?

    fun getAllCategories(): List<Category>

    fun deleteCategory(id: Int): Boolean
}
