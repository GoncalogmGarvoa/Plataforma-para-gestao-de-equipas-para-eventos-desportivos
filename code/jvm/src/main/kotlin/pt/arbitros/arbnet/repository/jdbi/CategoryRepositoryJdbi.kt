package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.springframework.context.annotation.ComponentScan
import pt.arbitros.arbnet.domain.universal.Category
import pt.arbitros.arbnet.repository.CategoryRepository

class CategoryRepositoryJdbi(
    private val handle: Handle
) : CategoryRepository {

    override fun createCategory(category: Category): Int {
        TODO("Not yet implemented")
    }

    override fun getCategoryNameById(id: Int): String? =
        handle
            .createQuery(
                """select name from dbp.category where id = :id""",
            ).bind("id", id)
            .mapTo<String>()
            .singleOrNull()

    override fun getCategoryIdByName(name: String): Int? =
        handle
            .createQuery(
                """select id from dbp.category where name = :name""",
            ).bind("name", name)
            .mapTo<Int>()
            .singleOrNull()


    override fun getAllCategories(): List<Category> {
        handle.
            createQuery(
                """select id, name, description from dbp.category""",
            )
            .mapTo<Category>()
            .list()
            .let { return it }
    }

    override fun deleteCategory(id: Int): Boolean {
        TODO("Not yet implemented")
    }
}
