package pt.arbitros.arbnet.repository

interface Transaction {
    val usersRepository: UsersRepository

    val competitionRepository: CompetitionRepository
    val callListRepository: CallListRepository
    val matchDayRepository: MatchDayRepository

    fun rollback()
}
