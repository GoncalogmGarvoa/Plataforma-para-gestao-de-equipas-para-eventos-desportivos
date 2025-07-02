package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.domain.Competition
import pt.arbitros.arbnet.repository.CompetitionRepository

class CompetitionRepositoryJdbi(
    private val handle: Handle,
) : CompetitionRepository {
    override fun createCompetition(
        competitionName: String,
        address: String,
        phoneNumber: String,
        email: String,
        association: String,
        location: String,
    ): Int =
        handle
            .createUpdate(
                """insert into dbp.competition (name, address, email, phone_number, location, association) values (:name, :address, :email, :phone_number, :location, :association)""",
            ).bind("name", competitionName)
            .bind("address", address)
            .bind("email", email)
            .bind("phone_number", phoneNumber)
            .bind("location", location)
            .bind("association", association)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun updateCompetition(
        id: Int,
        name: String,
        address: String,
        phoneNumber: String,
        email: String,
        location: String,
        association: String,
    ): Boolean =
        handle
            .createUpdate(
                """update dbp.competition set name = :name, address = :address, email = :email, phone_number = :phone_number, location = :location, association = :association where competition_number = :id""",
            ).bind("id", id)
            .bind("name", name)
            .bind("address", address)
            .bind("email", email)
            .bind("phone_number", phoneNumber)
            .bind("location", location)
            .bind("association", association)
            .execute() > 0

    override fun getCompetitionById(id: Int): Competition? =
        handle
            .createQuery("""select * from dbp.competition where competition_number = :id""")
            .bind("id", id)
            .mapTo<Competition>()
            .singleOrNull()

    override fun deleteCompetition(id: Int): Boolean {
        val rowsAffected = handle
            .createUpdate("""delete from dbp.competition where competition_number = :id""")
            .bind("id", id)
            .execute()
        return rowsAffected > 0
    }

    override fun getCompetitionIdByCallListId(callListId: Int): Int =
        handle
            .createQuery("""select competition_id from dbp.call_list where id = :callListId""")
            .bind("callListId", callListId)
            .mapTo<Int>()
            .one()
}

//
//    override fun getAllCompetitions(): List<Competition> =
//        handle
//            .createQuery("""select * from dbp.competitions""")
//            .mapTo<Competition>()
//            .list()
