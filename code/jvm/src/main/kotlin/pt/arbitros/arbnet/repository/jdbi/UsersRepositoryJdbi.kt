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
        iban: String,
    ): Int =
        handle
            .createUpdate(
                """insert into dbp.users (name, phone_Number, address, email, password, birth_date, iban) values (:name, :phone_number, :address ,:email, :password, :birth_date, :iban)""",
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

    override fun existsByEmailExcludingId(email: String, id: Int): Boolean =
        handle
            .createQuery("SELECT 1 FROM dbp.users WHERE email = :email AND id != :id")
            .bind("email", email)
            .bind("id", id)
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

    override fun existsByPhoneNumberExcludingId(phoneNumber: String, id: Int): Boolean =
        handle
            .createQuery("SELECT * FROM dbp.users WHERE phone_number = :phone_number and id != :id")
            .bind("phone_number", phoneNumber)
            .bind("id", id)
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

    override fun existsByIbanExcludingId(iban: String, id: Int): Boolean =
        handle
            .createQuery("SELECT * FROM dbp.users WHERE iban = :iban and id != :id")
            .bind("iban", iban)
            .bind("id", id)
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
        iban: String,
    ): Boolean =
        handle
            .createUpdate(
                """update dbp.users set name = :name, phone_number= :phoneNumber,address = :address, email = :email, password = :password, birth_date = :birth_date, iban = :iban where id = :id""",
            ).bind("name", name)
            .bind("phoneNumber", phoneNumber)
            .bind("address", address)
            .bind("email", email)
            .bind("password", password)
            .bind("birth_date", LocalDate.now())
            .bind("iban", iban)
            .bind("id", id)
            .execute() > 0

    override fun updateRoles(
        userId: Int,
        roles: List<String>,
    ): Boolean =
        handle
            .createUpdate("""UPDATE dbp.users SET roles = CAST(:roles AS text[]) WHERE id = :id""")
            .bindArray("roles", String::class.java, roles)
            .bind("id", userId)
            .execute() > 0

    override fun deleteUser(id: Int): Boolean =
        handle
            .createUpdate("""delete from dbp.users where id = :id""")
            .bind("id", id)
            .execute() > 0

    // todo check
    override fun userHasCouncilRole(userId: Int): Boolean =
        handle
            .createQuery(
                """
        SELECT 1 FROM dbp.users_roles ur
        JOIN dbp.role r ON ur.role_id = r.id
        WHERE ur.user_id = :user_id AND r.name = 'Arbitration_Council'
        """,
            ).bind("user_id", userId)
            .mapTo<Int>()
            .findFirst()
            .isPresent

    // todo check
    override fun getUsersAndCheckIfReferee(participants: List<Int>): List<Users> =
        handle
            .createQuery(
                """
        SELECT u.* FROM dbp.users u
        JOIN dbp.users_roles ur ON u.id = ur.user_id
        JOIN dbp.role r ON ur.role_id = r.id
        WHERE u.id IN (<participants>)
          AND r.name = 'Referee'
        """,
            ).bindList("participants", participants)
            .mapTo<Users>()
            .list()
}
