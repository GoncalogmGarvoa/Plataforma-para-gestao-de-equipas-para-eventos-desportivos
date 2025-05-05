package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.Users
import pt.arbitros.arbnet.repository.UsersRepository
import java.time.LocalDate

class UsersRepositoryJdbi(
    private val handle: Handle,
) : UsersRepository {
    override fun createUser(
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        password: String,
        birthDate: LocalDate,
        iban: String
    ): Int =
        handle
            .createUpdate(
                """insert into dbp.users (name, phone_Number, address, email, password, birth_date, iban, roles) values (:name, :phone_number, :address ,:email, :password, :birth_date, :iban, '{}')""",
            ).bind("name", name)
            .bind("phone_number", phoneNumber)
            .bind("address", address)
            .bind("email", email)
            .bind("password", password)
            .bind("birth_date", birthDate)
            .bind("iban", iban)
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


    override fun existsByPhoneNumber(phoneNumber: String): Boolean =
        handle
            .createQuery("SELECT * FROM dbp.users WHERE phone_number = :phone_number")
            .bind("phone_number", phoneNumber)
            .mapTo<Int>()
            .findFirst()
            .isPresent

    override fun existsByIban(iban: String): Boolean =
        handle
            .createQuery("SELECT * FROM dbp.users WHERE iban = :iban")
            .bind("iban", iban)
            .mapTo<Int>()
            .findFirst()
            .isPresent



    override fun updateUser(
        id: Int,
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        password: String,
        birthDate: LocalDate,
        iban: String
    ): Boolean =
        handle
            .createUpdate(
                """update dbp.users set name = :name, phone_Number= :phoneNumber,address = :address, email = :email, password = :password, birth_date = :birth_date, iban = :iban where id = :id""",
            ).bind("name", name)
            .bind("phone_Number", phoneNumber)
            .bind("address", address)
            .bind("email", email)
            .bind("password", password)
            .bind("birth_date", LocalDate.now())
            .bind("iban", iban)
            .execute() > 0

    override fun updateRoles(
        id: Int,
        roles: List<String>,
    ): Boolean =
        handle
            .createUpdate("""UPDATE dbp.users SET roles = CAST(:roles AS text[]) WHERE id = :id""")
            .bindArray("roles", String::class.java, roles)
            .bind("id", id)
            .execute() > 0

    override fun deleteUser(id: Int): Boolean =
        handle
            .createUpdate("""delete from dbp.users where id = :id""")
            .bind("id", id)
            .execute() > 0

    override fun userHasCouncilRole(councilUserId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun getUsersAndCheckIfReferee(participants: List<Int>): List<Users> {
        TODO("Not yet implemented")
    }
}
// val rolesArray = handle.connection.createArrayOf("text", roles.toTypedArray())
