@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.repository.jdbi

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.jdbi.v3.core.statement.StatementContext
import pt.arbitros.arbnet.domain.adaptable.Position
import pt.arbitros.arbnet.domain.adaptable.Role
import pt.arbitros.arbnet.domain.users.*
import pt.arbitros.arbnet.http.model.users.UserCategoryHistoryOutputModel
import pt.arbitros.arbnet.repository.UsersRepository
import java.sql.ResultSet
import java.time.LocalDate

class UsersRepositoryJdbi(
    private val handle: Handle,
) : UsersRepository {
    override fun getTokenAndUserByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Pair<User, Token>? =
        handle
            .createQuery(
                """
                select id, phone_number, address ,name ,email ,password_validation, birth_date,iban, status ,token_validation, created_at, last_used_at
                from dbp.Users as users 
                inner join dbp.tokens as tokens 
                on users.id = tokens.user_id
                where token_validation = :validation_information
            """,
            ).bind("validation_information", tokenValidationInfo.validationInfo)
            .mapTo<UserAndTokenModel>()
            .singleOrNull()?.userAndToken

    override fun getTokenByTokenValidationInfo(tokenValidationInfo: TokenValidationInfo): Token? =
        handle
            .createQuery(
                """
                select * from dbp.tokens where token_validation = :validation_information
            """,
            ).bind("validation_information", tokenValidationInfo.validationInfo)
            .mapTo<TokenModel>()
            .singleOrNull()?.toToken()

    private data class TokenModel(
        @ColumnName("token_validation")
        val tokenValidation: String,

        @ColumnName("user_id")
        val userId: Int,

        @ColumnName("created_at")
        val createdAt: Long,

        @ColumnName("last_used_at")
        val lastUsedAt: Long,
    ) {
        fun toToken(): Token = Token(
            tokenValidationInfo = TokenValidationInfo(tokenValidation),
            userId = userId,
            createdAt = Instant.fromEpochSeconds(createdAt),
            lastUsedAt = Instant.fromEpochSeconds(lastUsedAt),
        )
    }

    override fun assignRoleToUserToToken(
        userId: Int,
        tokenValidationInfo: TokenValidationInfo,
        roleId: Int,
    ): Boolean =
        handle
            .createUpdate(
                """
            INSERT INTO dbp.user_token_role (user_id, token_val, role_id)
            VALUES (:user_id, :token_v, :role_id)
            ON CONFLICT DO NOTHING
        """,
            ).bind("user_id", userId)
            .bind("token_v", tokenValidationInfo.validationInfo)
            .bind("role_id", roleId)
            .execute() > 0




    private data class UserAndTokenModel(
        val id: Int,
        val phoneNumber: String,
        val address: String,
        val name: String,
        val email: String,
        val passwordValidation: PasswordValidationInfo,
        val birthDate: LocalDate,
        val iban: String,
        val status: String,
        val tokenValidation: TokenValidationInfo,
        val createdAt: Long,
        val lastUsedAt: Long,
    ) {
        val status2 =
            UserStatus.entries.firstOrNull { it.status == status }
                ?: throw IllegalArgumentException("Invalid user status: $status")

        val userAndToken: Pair<User, Token>
            get() =
                Pair(
                    User(id, phoneNumber, address, name, email, passwordValidation, birthDate, iban, status2),
                    Token(
                        tokenValidation,
                        id,
                        Instant.fromEpochSeconds(createdAt),
                        Instant.fromEpochSeconds(lastUsedAt),
                    ),
                )
    }

    override fun createToken(
        token: Token,
        maxTokens: Int,
    ) {
        // Primeiro apaga os registros relacionados na tabela user_token_role
        handle.createUpdate(
            """
        delete from dbp.user_token_role
        where token_val in (
            select token_validation from dbp.tokens
            where user_id = :user_id
            order by last_used_at desc
            offset :offset
        )
        """.trimIndent()
        ).bind("user_id", token.userId)
            .bind("offset", maxTokens - 1)
            .execute()


        handle.createUpdate(
            """
        delete from dbp.tokens
        where user_id = :user_id
        and token_validation in (
            select token_validation from dbp.tokens
            where user_id = :user_id
            order by last_used_at desc
            offset :offset
        )
        """.trimIndent()
        ).bind("user_id", token.userId)
            .bind("offset", maxTokens - 1)
            .execute()

        // Insere o novo token
        handle.createUpdate(
            """
        insert into dbp.tokens(user_id, token_validation, created_at, last_used_at)
        values (:user_id, :token_validation, :created_at, :last_used_at)
        """.trimIndent()
        ).bind("user_id", token.userId)
            .bind("token_validation", token.tokenValidationInfo.validationInfo)
            .bind("created_at", token.createdAt.epochSeconds)
            .bind("last_used_at", token.lastUsedAt.epochSeconds)
            .execute()
    }




    override fun updateTokenLastUsed(
        token: Token,
        now: Instant,
    ) {
        handle
            .createUpdate(
                """
                update dbp.Tokens
                set last_used_at = :last_used_at
                where token_validation = :validation_information
                """.trimIndent(),
            ).bind("last_used_at", now.epochSeconds)
            .bind("validation_information", token.tokenValidationInfo.validationInfo)
            .execute()
    }

    override fun removeTokenByValidationInfo(tokenValidationInfo: TokenValidationInfo): Int =
        handle
            .createUpdate(
                """
                delete from dbp.Tokens
                where token_validation = :validation_information
            """,
            ).bind("validation_information", tokenValidationInfo.validationInfo)
            .execute()

    override fun getUserByToken(tokenValidationInfo: TokenValidationInfo): User? {
        return handle
            .createQuery(
                """
                select * from dbp.users as u
                inner join dbp.tokens as t on u.id = t.user_id
                where t.token_validation = :token
            """,
            ).bind("token", tokenValidationInfo.validationInfo)
            .map(UsersMapper())
            .singleOrNull()
    }

    override fun createUser(
        name: String,
        phoneNumber: String,
        address: String,
        email: String,
        passwordValidation: PasswordValidationInfo,
        birthDate: LocalDate,
        iban: String,
    ): Int =
        handle
            .createUpdate(
                """insert into dbp.users (name, phone_Number, address, email, password_validation, birth_date, iban) values (:name, :phone_number, :address ,:email, :password_validation, :birth_date, :iban)""",
            ).bind("name", name)
            .bind("phone_number", phoneNumber)
            .bind("address", address)
            .bind("email", email)
            .bind("password_validation", passwordValidation.validationInfo)
            .bind("birth_date", birthDate)
            .bind("iban", iban)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun getUserById(id: Int): User? =
        handle
            .createQuery("""select * from dbp.users where id = :id""")
            .bind("id", id)
            .map { rs, _ ->
                usersMap(rs)
            }.singleOrNull()

    override fun getUserByEmail(email: String): User? =
        handle
            .createQuery("""select * from dbp.users where email = :email""")
            .bind("email", email)
            .map { rs, _ ->
                usersMap(rs)
            }.singleOrNull()

    override fun getUsersByName(name: String): List<User> =
        handle
            .createQuery("""
            SELECT * FROM dbp.users 
            WHERE LOWER(name) LIKE LOWER(:name)
        """)
            .bind("name", "%$name%")
            .map { rs, _ -> usersMap(rs) }
            .list()

    override fun getUsersByParameters(name: String, roles: List<String>): List<User> {
        val namePattern = "%${name.lowercase()}%"
        return if (roles.isEmpty()) {
            handle
                .createQuery(
                    """
                SELECT DISTINCT u.* FROM dbp.users u
                WHERE LOWER(u.name) LIKE :name AND u.status = 'active'
                """,
                )
                .bind("name", namePattern)
                .map(UsersMapper())
                .list()
        } else {
            handle
                .createQuery(
                    """
                SELECT DISTINCT u.* FROM dbp.users u
                JOIN dbp.users_roles ur ON u.id = ur.user_id
                JOIN dbp.role r ON ur.role_id = r.id
                WHERE LOWER(u.name) LIKE :name
                  AND r.name IN (<roles>) AND u.status = 'active'
                """,
                )
                .bind("name", namePattern)
                .bindList("roles", roles)
                .map(UsersMapper())
                .list()
        }
    }


    override fun getUsersWithoutRoles(name: String): List<User> =
        handle
            .createQuery(
                """
            SELECT * FROM dbp.users u
            WHERE LOWER(u.name) LIKE LOWER(:name)
              AND NOT EXISTS (
                SELECT 1 FROM dbp.users_roles ur WHERE ur.user_id = u.id
            )
        """,
            ).bind("name", "%$name%")
            .map(UsersMapper())
            .list()


    private fun usersMap(rs: ResultSet) =
        User(
            id = rs.getInt("id"),
            phoneNumber = rs.getString("phone_number"),
            address = rs.getString("address"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            passwordValidation = PasswordValidationInfo(rs.getString("password_validation")),
            birthDate = rs.getDate("birth_date").toLocalDate(),
            iban = rs.getString("iban"),
            userStatus = UserStatus.valueOf(rs.getString("status").uppercase()), // ou fromString()
        )

    class UsersMapper : RowMapper<User> {
        override fun map(
            rs: ResultSet,
            ctx: StatementContext?,
        ): User =
            User(
                id = rs.getInt("id"),
                phoneNumber = rs.getString("phone_number"),
                address = rs.getString("address"),
                name = rs.getString("name"),
                email = rs.getString("email"),
                passwordValidation = PasswordValidationInfo(rs.getString("password_validation")),
                birthDate = rs.getDate("birth_date").toLocalDate(),
                iban = rs.getString("iban"),
                userStatus = UserStatus.values().firstOrNull { it.status == rs.getString("status") }
                    ?: throw IllegalArgumentException("Invalid user status: ${rs.getString("status")}")
            )
    }


    override fun existsByEmail(email: String): Boolean =
        handle
            .createQuery("SELECT * FROM dbp.users WHERE email = :email")
            .bind("email", email)
            .mapTo<Int>()
            .findFirst()
            .isPresent

    override fun existsByEmailExcludingId(
        email: String,
        id: Int,
    ): Boolean =
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

    override fun existsByPhoneNumberExcludingId(
        phoneNumber: String,
        id: Int,
    ): Boolean =
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

    override fun existsByIbanExcludingId(
        iban: String,
        id: Int,
    ): Boolean =
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
        password: PasswordValidationInfo,
        birthDate: LocalDate,
        iban: String,
    ): Boolean =
        handle
            .createUpdate(
                """update dbp.users set name = :name, phone_number= :phoneNumber,address = :address, email = :email, password_validation = :password, birth_date = :birth_date, iban = :iban where id = :id""",
            ).bind("name", name)
            .bind("phoneNumber", phoneNumber)
            .bind("address", address)
            .bind("email", email)
            .bind("password", password.validationInfo)
            .bind("birth_date", birthDate)
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

    override fun getInactiveUsers(): List<User> =
        handle
            .createQuery(
                """
            SELECT * FROM dbp.users WHERE status = 'inactive' AND id IN (
                SELECT user_id FROM dbp.users_roles
            )
        """,
            )
            .map(UsersMapper())
            .list()

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
    override fun getUsersAndCheckIfReferee(participants: List<Int>): List<User> =
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
            .map { rs, _ ->
                usersMap(rs)
            }
            .list()

    override fun areAllUsersActive(userIds: List<Int>): Boolean {
        return handle
            .createQuery(
                """
            SELECT COUNT(*) FROM dbp.users WHERE id IN (<userIds>) AND status = 'active'
        """,
            ).bindList("userIds", userIds)
            .mapTo<Int>()
            .single() == userIds.size

    }

    override fun updateUserStatus(id: Int, status: String): Boolean {
        return handle
            .createUpdate(
                """
            UPDATE dbp.users
            SET status = :status
            WHERE id = :id
        """,
            ).bind("status", status)
            .bind("id", id)
            .execute() > 0
    }



    override fun getAllUsers(): List<User> {
        return handle
            .createQuery("SELECT * FROM dbp.users")
            .map(UsersMapper())
            .list()
    }

    override fun getUserCategoryHistory(userId: Int): List<UserCategoryHistoryOutputModel> =
        handle
            .createQuery(
                """
            SELECT c.id AS category_id, c.name, cd.start_date, cd.end_date
            FROM dbp.category_dir cd
            JOIN dbp.category c ON cd.category_id = c.id
            WHERE cd.user_id = :userId
            ORDER BY cd.start_date DESC
            """
            )
            .bind("userId", userId)
            .map { rs, _ ->
                UserCategoryHistoryOutputModel(
                    categoryId = rs.getInt("category_id"),
                    categoryName = rs.getString("name"),
                    startDate = rs.getDate("start_date").toLocalDate(),
                    endDate = rs.getDate("end_date")?.toLocalDate()
                )
            }
            .list()

    override fun getAllPositions(): List<Position> =
        handle
            .createQuery("SELECT * FROM dbp.positions")
            .map { rs, _ ->
                Position(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                )
            }
            .list()


}
