package pt.arbitros.arbnet.repository.jdbi

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.arbitros.arbnet.repository.CompetitionRepository

class CompetitionRepositoryJdbi(
    private val handle: Handle,
) : CompetitionRepository {
    override fun createCompetition(
        competitionName: String,
        address: String,
        phoneNumber: Int,
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

//        handle
//            .createUpdate(
//                """insert into dbp.competitions (name, address, email, phone_number, location, association) values (:name, :address, :email, :phone_number, :location, :association)""",
//            ).bind(1, name)
//            .bind(2, address)
//            .bind(3, email)
//            .bind(4, phoneNumber)
//            .bind(5, location)
//            .bind(6, association)
//            .executeAndReturnGeneratedKeys()
//            .mapTo<Int>()
//            .one()
//
//    override fun findCompetitionById(id: Int): Competition? =
//        handle
//            .createQuery("""select * from dbp.competitions where id = :id""")
//            .bind("id", id)
//            .mapTo<Competition>()
//            .singleOrNull()
//
//    override fun getAllCompetitions(): List<Competition> =
//        handle
//            .createQuery("""select * from dbp.competitions""")
//            .mapTo<Competition>()
//            .list()
//
//    override fun updateCompetition(
//        id: Int,
//        name: String,
//        address: String,
//        email: String,
//        phoneNumber: String,
//        location: String,
//        association: String,
//    ): Boolean =
//        handle
//            .createUpdate(
//                """update dbp.competitions set name = :name, address = :address, email = :email, phone_number = :phone_number, location = :location, association = :association where id = :id""",
//            ).bind("id", id)
//            .bind("name", name)
//            .bind("address", address)
//            .bind("email", email)
//            .bind("phone_number", phoneNumber)
//            .bind("location", location)
//            .bind("association", association)
//            .execute() > 0
//
//    override fun deleteCompetition(id: Int): Boolean =
//        handle
//            .createUpdate("""delete from dbp.competitions where id = :id""")
//            .bind("id", id)
//            .execute() > 0
}
