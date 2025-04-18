package pt.arbitros.arbnet.repository.jdbi

import org.apache.catalina.User
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.repository.UsersRepository
import java.sql.ResultSet
import java.time.LocalDate

class JdbiUsersRepository (
    private val handle: Handle
) : UsersRepository {
    override fun createUser(user: Users): Int {
        TODO("Not yet implemented")
    }

    override fun getUserById(id: Int): Users? = handle.createQuery("""select * from dbp.users where id = :id""")
            .bind("id", id)
            .mapTo<Users>()
            .singleOrNull()


    override fun findUserByEmail(email: String): Users? {
        TODO("Not yet implemented")
    }

    override fun existsByEmail(email: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun updateUser(user: Users): Boolean {
        TODO("Not yet implemented")
    }

    override fun deleteUser(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun findRoles(user: Users): List<String> {
        TODO("Not yet implemented")
    }

    override fun findIban(user: Users): Int {
        TODO("Not yet implemented")
    }

    override fun updateIban(user: Users): Boolean {
        TODO("Not yet implemented")
    }

}
