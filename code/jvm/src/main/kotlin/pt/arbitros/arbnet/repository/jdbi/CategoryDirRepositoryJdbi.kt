package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.CategoryDir
import pt.arbitros.arbnet.domain.universal.Category
import pt.arbitros.arbnet.repository.CategoryDirRepository

class CategoryDirRepositoryJdbi(
    private val handle: Handle
) : CategoryDirRepository {

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

    override fun getCategoryIdByUserId(userId: Int): Int? =
        handle
            .createQuery("""
                    select category_id from dbp.category_dir
                    where user_id = :userId and end_date is null""")
            .bind("userId", userId)
            .mapTo<Int>()
            .singleOrNull()

    override fun updateUserCategory(userId: Int, categoryId: Int): Boolean =

        handle.inTransaction<Boolean, Exception> { h ->
            h.createUpdate(
                """
                update dbp.category_dir
                set end_date = CURRENT_DATE
                where user_id = :userId and end_date is null"""
            ).bind("userId", userId).execute()

            h.createUpdate(
                """
                insert into dbp.category_dir (user_id, category_id, start_date)
                values (:userId, :categoryId, CURRENT_DATE)"""
            ).bind("userId", userId)
                .bind("categoryId", categoryId)
                .execute() > 0
        }

    override fun removeCategoryDirEntry(refereeId: Int, categoryId: Int, id: Int): Boolean {
        TODO("Not yet implemented")
    }

}
