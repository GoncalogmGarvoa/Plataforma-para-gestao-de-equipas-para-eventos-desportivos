package pt.arbitros.arbnet.repository.adaptable_repos

import pt.arbitros.arbnet.domain.adaptable.Category

interface CategoryRepository {

    fun getCategoryNameById(id: Int): String?

    fun getCategoryIdByName(name: String): Int?

    fun verifyCategoryIds(ids: List<Int>): Boolean

    fun getAllCategories(): List<Category>
}