package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.repository.UsersRepository
import java.time.LocalDate

class JdbiUsersRepository(
    private val handle: Handle,
) : UsersRepository {
    override fun createUser(
        name: String,
        email: String,
        password: String,
        birthDate: LocalDate,
        iban: String,
    ): Int =
        handle
            .createUpdate(
                """insert into dbp.users (name, email, password, birth_date, iban, roles) values (:name, :email, :password, :birth_date, :iban, :roles)""",
            ).bind("name", name)
            .bind("email", email)
            .bind("password", password)
            .bind("birth_date", birthDate)
            .bind("iban", iban)
            .bind("roles", "")
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun getUserById(id: Int): Users? =
        handle
            .createQuery("""select * from dbp.users where id = :id""")
            .bind("id", id)
            .mapTo<Users>()
            .singleOrNull()

    override fun getUserByEmail(email: String): Users? =
        handle
            .createQuery("""select * from dbp.users where email = :email""")
            .bind("email", email)
            .mapTo<Users>()
            .singleOrNull()

    override fun existsByEmail(email: String): Boolean =
        handle
            .createQuery("SELECT * FROM dbp.users WHERE email = :email")
            .bind("email", email)
            .mapTo<Int>()
            .findFirst()
            .isPresent

    override fun updateUser(
        name: String,
        email: String,
        password: String,
        birthDate: LocalDate,
        iban: String,
    ): Boolean =
        handle
            .createUpdate(
                """update dbp.users set name = :name, email = :email, password = :password, birth_date = :birth_date, iban = :iban where email = :email""",
            ).bind("name", name)
            .bind("email", email)
            .bind("password", password)
            .bind("birth_date", LocalDate.now())
            .bind("iban", iban)
            .execute() > 0

    override fun deleteUser(id: Int): Boolean =
        handle
            .createUpdate("""delete from dbp.users where id = :id""")
            .bind("id", id)
            .execute() > 0
}
