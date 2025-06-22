package pt.arbitros.arbnet.repository.adaptable_repos

import pt.arbitros.arbnet.domain.universal.Category

interface CategoryRepository {

    fun getCategoryNameById(id: Int): String?

    fun getCategoryIdByName(name: String): Int?

    fun getAllCategories(): List<Category>
}