package pt.arbitros.arbnet.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.InjectMocks
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.*
import org.mockito.quality.Strictness
import pt.arbitros.arbnet.domain.Session
import pt.arbitros.arbnet.domain.SessionReferee
import pt.arbitros.arbnet.domain.adaptable.Position
import pt.arbitros.arbnet.domain.users.User
import pt.arbitros.arbnet.http.ApiError
import pt.arbitros.arbnet.http.model.SessionRefereeInputModel
import pt.arbitros.arbnet.repository.*
import pt.arbitros.arbnet.repository.adaptable_repos.PositionRepository
import pt.arbitros.arbnet.repository.auxiliary.SessionRefereesRepository
import java.time.LocalDateTime
import java.time.LocalTime

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension::class)
class SessionServiceTest {

    // Mock the transaction context with all needed repositories
    interface TransactionContext : Transaction {
        override val sessionsRepository: SessionsRepository
        override val usersRepository: UsersRepository
        override val positionRepository: PositionRepository
        override val sessionRefereesRepository: SessionRefereesRepository
    }

    @Mock lateinit var sessionsRepository: SessionsRepository
    @Mock lateinit var usersRepository: UsersRepository
    @Mock lateinit var positionRepository: PositionRepository
    @Mock lateinit var sessionRefereesRepository: SessionRefereesRepository


    @Mock
    lateinit var transactionManager: TransactionManager

    @Mock
    lateinit var txContext: TransactionContext

    @InjectMocks
    lateinit var sessionService: SessionService

    @BeforeEach
    fun setup() {
        whenever(txContext.sessionsRepository).thenReturn(sessionsRepository)
        whenever(txContext.usersRepository).thenReturn(usersRepository)
        whenever(txContext.positionRepository).thenReturn(positionRepository)
        whenever(txContext.sessionRefereesRepository).thenReturn(sessionRefereesRepository)
    }

    @Test
    fun `finishSession returns success true when session exists and finishSession succeeds`() {
        val sessionId = 42
        val session = Session(
            id = sessionId,
            matchDayId = 1,
            competitionIdMatchDay = 1,
            startTime = LocalTime.now(),
            endTime = LocalTime.now().plusHours(1),
        )

        // Mock transactionManager.run lambda call
        whenever(transactionManager.run<Boolean>(any())).thenAnswer { invocation ->
            val block = invocation.getArgument<(TransactionContext) -> Either<ApiError, Boolean>>(0)
            block(txContext)
        }

        // Usa diretamente os mocks já configurados no setup
        whenever(sessionsRepository.getSessionById(sessionId)).thenReturn(session)
        whenever(sessionsRepository.finishSession(sessionId)).thenReturn(true)

        val result = sessionService.finishSession(sessionId)

        assertTrue(result is Success)
        assertEquals(true, (result as Success).value)

        verify(sessionsRepository).getSessionById(sessionId)
        verify(sessionsRepository).finishSession(sessionId)
    }

    @Test
    fun `finishSession returns failure when session not found`() {
        val sessionId = 42

        whenever(transactionManager.run<Boolean>(any())).thenAnswer { invocation ->
            val block = invocation.getArgument<(TransactionContext) -> Either<ApiError, Boolean>>(0)
            block(txContext)
        }
        whenever(txContext.sessionsRepository.getSessionById(sessionId)).thenReturn(null)

        val result = sessionService.finishSession(sessionId)

        assertTrue(result is Failure)
        assertEquals(404, (result as Failure).value.status)

        verify(txContext.sessionsRepository).getSessionById(sessionId)
        verify(txContext.sessionsRepository, never()).finishSession(anyInt())
    }

    @Test
    fun `updateSessionReferees returns success when all entities found and update succeeds`() {
        // Prepare input
        val inputList = listOf(
            SessionRefereeInputModel(sessionId = 10, positionId = 20, userId = 30)
        )

        val session = Session(
            id = 10,
            matchDayId = 100,
            competitionIdMatchDay = 200,
            startTime = LocalTime.now(),
            endTime = LocalTime.now().plusHours(1),)
        val user = mock<User>() // or a real User object
        val position = mock<Position>() // or real Position object

        // Mock transactionManager.run
        whenever(transactionManager.run<Boolean>(any())).thenAnswer { invocation ->
            val block = invocation.getArgument<(TransactionContext) -> Either<ApiError, Boolean>>(0)
            block(txContext)
        }

        // Mock repository calls
        whenever(txContext.usersRepository.getUsersAndCheckIfReferee(any())).thenReturn(listOf(user))
        whenever(txContext.sessionsRepository.getSessionById(10)).thenReturn(session)
        whenever(txContext.usersRepository.getUserById(30)).thenReturn(user)
        whenever(txContext.positionRepository.getPositionById(20)).thenReturn(position)
        whenever(txContext.sessionRefereesRepository.updateSessionReferees(any())).thenReturn(true)

        val result = sessionService.updateSessionReferees(inputList)

        assertTrue(result is Success)
        assertEquals(true, (result as Success).value)

        verify(txContext.usersRepository).getUsersAndCheckIfReferee(listOf(30))
        verify(txContext.sessionsRepository).getSessionById(10)
        verify(txContext.usersRepository).getUserById(30)
        verify(txContext.positionRepository).getPositionById(20)
        verify(txContext.sessionRefereesRepository).updateSessionReferees(any())
    }

    @Test
    fun `updateSessionReferees returns failure when session not found`() {
        val inputList = listOf(
            SessionRefereeInputModel(sessionId = 10, positionId = 20, userId = 30)
        )

        whenever(transactionManager.run<Boolean>(any())).thenAnswer { invocation ->
            val block = invocation.getArgument<(TransactionContext) -> Either<ApiError, Boolean>>(0)
            block(txContext)
        }

        whenever(txContext.sessionsRepository.getSessionById(10)).thenReturn(null)

        val result = sessionService.updateSessionReferees(inputList)

        assertTrue(result is Failure)
        assertEquals(404, (result as Failure).value.status)

        verify(txContext.sessionsRepository).getSessionById(10)
        verify(txContext.usersRepository, never()).getUserById(anyInt())
        verify(txContext.positionRepository, never()).getPositionById(anyInt())
        verify(txContext.sessionRefereesRepository, never()).updateSessionReferees(any())
    }

}
