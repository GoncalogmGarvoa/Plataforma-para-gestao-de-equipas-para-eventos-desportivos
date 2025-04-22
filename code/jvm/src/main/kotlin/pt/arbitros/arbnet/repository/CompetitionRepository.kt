package pt.arbitros.arbnet.repository

interface CompetitionRepository {
    fun createCompetition(
        competitionName: String,
        competitionNumber: Int,
        address: String,
        phoneNumber: Int,
        email: String,
        association: String,
        location: String,
    ): Int

//    fun findCompetitionById(id: Int): Competition?
//
//    fun getAllCompetitions(): List<Competition>
//
//    fun updateCompetition(
//        id: Int,
//        name: String,
//        address: String,
//        email: String,
//        phoneNumber: String,
//        location: String,
//        association: String,
//    ): Boolean
//
//    fun deleteCompetition(id: Int): Boolean
}
