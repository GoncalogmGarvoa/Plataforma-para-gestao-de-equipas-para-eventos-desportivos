package pt.arbitros.arbnet.repository.jdbi.adaptable_repos

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.adaptable.Category
import pt.arbitros.arbnet.repository.adaptable_repos.CategoryRepository

class CategoryRepositoryJdbi(
    private val handle: Handle
) : CategoryRepository {

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

    override fun verifyCategoryIds(ids: List<Int>): Boolean {
        val sql = """
        select count(*) from dbp.category
        where id in (<IDs>)
        """

        val count = handle.createQuery(sql)
            .bindList("IDs", ids)
            .mapTo(Int::class.java)
            .one()

        return count == ids.size
    }


    override fun getAllCategories(): List<Category> {
        handle.
            createQuery(
                """select * from dbp.category order by name""",
            )
            .mapTo<Category>()
            .list()
            .let { categories ->
                return categories.sortedBy { it.name }
            }
    }

    override fun verifyCategoryNames(names: List<String>): Boolean {
        val sql = """
        select count(*) from dbp.category
        where name in (<NAMES>)
        """

        val count = handle.createQuery(sql)
            .bindList("NAMES", names)
            .mapTo(Int::class.java)
            .one()

        return count == names.size
    }

}