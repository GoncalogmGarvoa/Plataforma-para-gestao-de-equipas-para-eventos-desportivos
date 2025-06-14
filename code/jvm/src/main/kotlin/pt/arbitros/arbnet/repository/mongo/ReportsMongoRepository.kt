package pt.arbitros.arbnet.repository.mongo

import org.springframework.data.mongodb.repository.MongoRepository
import pt.arbitros.arbnet.domain.ReportMongo
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import pt.arbitros.arbnet.domain.PaymentReportMongo


// This interface defines custom MongoDB operations that are not provided by the default MongoRepository.
interface CustomMongoReportFunctions {
    fun seal(id: String): Boolean
}

class CustomMongoReportFunctionsImpl (
    private val mongoTemplate: MongoTemplate
) : CustomMongoReportFunctions {

    override fun seal(id: String): Boolean {
        val query = Query(Criteria.where("_id").`is`(id))
        val update = Update().set("sealed", true)
        val result = mongoTemplate.updateFirst(query, update, ReportMongo::class.java)
        return result.modifiedCount > 0
    }
}

// This interface extends the MongoRepository interface provided by Spring Data MongoDB.
// It allows us to perform CRUD operations on the Report entity without writing any boilerplate code.
// The Report entity is expected to have a String type ID.
interface ReportMongoRepository : MongoRepository<ReportMongo, String>, CustomMongoReportFunctions

interface PaymentReportMongoRepository : MongoRepository<PaymentReportMongo, String>, CustomMongoReportFunctions