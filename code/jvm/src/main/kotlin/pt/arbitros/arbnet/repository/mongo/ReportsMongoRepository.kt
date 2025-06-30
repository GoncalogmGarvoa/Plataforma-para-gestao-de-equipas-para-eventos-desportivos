package pt.arbitros.arbnet.repository.mongo

import com.mongodb.client.result.UpdateResult
import org.springframework.data.mongodb.repository.MongoRepository
import pt.arbitros.arbnet.domain.ReportMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import pt.arbitros.arbnet.domain.PaymentReportMongo


// This interface defines custom MongoDB operations that are not provided by the default MongoRepository.
interface CustomMongoFunctions {
    fun seal(id: String, reportOrPayment: Boolean): Boolean
    fun findByCompetitionId(competitionId: Int, reportOrPayment: Boolean): List<Any>
}

class CustomMongoFunctionsImpl (
    private val mongoTemplate: MongoTemplate
) : CustomMongoFunctions {

    override fun seal(id: String, reportOrPayment : Boolean): Boolean {
        val query = Query(Criteria.where("_id").`is`(id))
        val update = Update().set("sealed", true)

        val result : UpdateResult
        if (reportOrPayment)
            result = mongoTemplate.updateFirst(query, update, ReportMongo::class.java)
        else
            result = mongoTemplate.updateFirst(query, update, PaymentReportMongo::class.java)
        return result.modifiedCount > 0
    }

    override fun findByCompetitionId(competitionId: Int, reportOrPayment: Boolean): List<Any> {
        val query = Query(Criteria.where("competitionId").`is`(competitionId))
        return if (reportOrPayment)
            mongoTemplate.find(query, ReportMongo::class.java)
        else
            mongoTemplate.find(query, PaymentReportMongo::class.java)

    }
}

// This interface extends the MongoRepository interface provided by Spring Data MongoDB.
// It allows us to perform CRUD operations on the Report entity without writing any boilerplate code.
// The Report entity is expected to have a String type ID.
interface ReportMongoRepository : MongoRepository<ReportMongo, String>, CustomMongoFunctions

interface PaymentReportMongoRepository : MongoRepository<PaymentReportMongo, String>, CustomMongoFunctions