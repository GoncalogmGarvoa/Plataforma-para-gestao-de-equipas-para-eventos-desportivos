@file:Suppress("ktlint:standard:no-wildcard-imports")

package pt.arbitros.arbnet.services

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import pt.arbitros.arbnet.Environment
import pt.arbitros.arbnet.domain.UsersDomain
import pt.arbitros.arbnet.repository.TransactionManager
import pt.arbitros.arbnet.repository.UsersRepository
import pt.arbitros.arbnet.repository.mem.UsersRepositoryMem
import pt.arbitros.arbnet.repositoryJdbi.TransactionManagerJdbi
import pt.arbitros.arbnet.repositoryJdbi.configureWithAppRequirements
import java.util.stream.Stream

// todo tests in mem
/*
val usersRepository: UsersRepository = UsersRepositoryMem()

@Suppress("ktlint:standard:no-consecutive-comments")
class UserServiceTests {
    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()

        @JvmStatic
        fun transactionManagers(): Stream<TransactionManager> =
            Stream.of(
                // TransactionManager().also { cleanup(it) },
                TransactionManagerJdbi(jdbi).also { cleanup(it) },
            )

        private fun cleanup(trxManager: TransactionManager) {
            trxManager.run {
                usersRepository.clear()
            }
        }

        private fun createUsersService(trxManager: TransactionManager): UsersService = UsersService(trxManager, UsersDomain())
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `should create and fetch user by id`(trxManager: TransactionManager) {
        val service = createUsersService(trxManager)

        val userId =
            service.createUser(
                name = "João Silva",
                phoneNumber = "912345678",
                address = "Rua A, Lisboa",
                email = "joao@example.com",
                password = "secure123",
                birthDate = "1990-01-01",
                iban = "PT50000201231234567890154",
            )

        val user = service.getUserById(userId)

        assertEquals("João Silva", user.name)
        assertEquals("joao@example.com", user.email)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `should update user`(trxManager: TransactionManager) {
        val service = createUsersService(trxManager)

        service.createUser(
            name = "Maria",
            phoneNumber = 911111111,
            address = "Rua B",
            email = "maria@example.com",
            password = "pass123",
            birthDate = "1995-05-05",
            iban = "PT50000201231234567890155",
        )

        val updated =
            service.updateUser(
                name = "Maria Atualizada",
                phoneNumber = 922222222,
                address = "Rua C",
                email = "maria@example.com",
                password = "newpass",
                birthDate = "1995-05-05",
                iban = "PT50000201231234567890155",
            )

        val user = service.getUserByEmail("maria@example.com")
        assertTrue(updated)
        assertEquals("Maria Atualizada", user.name)
        assertEquals(922222222, user.phoneNumber)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `should delete user`(trxManager: TransactionManager) {
        val service = createUsersService(trxManager)

        val userId =
            service.createUser(
                name = "Carlos",
                phoneNumber = 933333333,
                address = "Rua D",
                email = "carlos@example.com",
                password = "deleteMe",
                birthDate = "1985-12-12",
                iban = "PT50000201231234567890156",
            )

        val deleted = service.deleteUser(userId)
        assertTrue(deleted)

        val exception =
            assertThrows(Exception::class.java) {
                service.getUserById(userId)
            }
        assertTrue(exception.message!!.contains("not found"))
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `should validate user existence by email`(trxManager: TransactionManager) {
        val service = createUsersService(trxManager)
        service.createUser(
            name = "Ana",
            phoneNumber = 944444444,
            address = "Rua E",
            email = "ana@example.com",
            password = "exists123",
            birthDate = "1992-03-03",
            iban = "PT50000201231234567890157",
        )

        assertTrue(service.existsByEmail("ana@example.com"))
        assertFalse(service.existsByEmail("nonexistent@example.com"))
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `should add and remove role`(trxManager: TransactionManager) {
        val service = createUsersService(trxManager)
        val id =
            service.createUser(
                name = "Rita",
                phoneNumber = 955555555,
                address = "Rua F",
                email = "rita@example.com",
                password = "roleUser",
                birthDate = "1980-07-07",
                iban = "PT50000201231234567890158",
            )

        val added = service.updateRoles(id, "ADMIN", addOrRemove = true)
        assertTrue(added)

        val removed = service.updateRoles(id, "ADMIN", addOrRemove = false)
        assertTrue(removed)
    }
}

*/