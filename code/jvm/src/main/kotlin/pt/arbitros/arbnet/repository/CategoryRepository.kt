package pt.arbitros.arbnet.repository

import pt.arbitros.arbnet.domain.universal.Category

interface CategoryRepository {
    fun createCategory(category: Category): Int

    fun getCategoryNameById(id: Int): String?

    fun getCategoryIdByName(name: String): Int?

    fun getAllCategories(): List<Category>

    fun deleteCategory(id: Int): Boolean
}