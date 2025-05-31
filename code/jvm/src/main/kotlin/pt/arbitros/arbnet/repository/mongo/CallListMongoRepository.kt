package pt.arbitros.arbnet.repository.mongo

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository
import pt.arbitros.arbnet.domain.CallListDocument

interface CustomMongoCallListFunctions {
    fun findByIntegerId(id: Int): CallListDocument?
}

class CustomMongoCallListFunctionsImpl (
    private val mongoTemplate: MongoTemplate
) : CustomMongoCallListFunctions {

    override fun findByIntegerId(id: Int): CallListDocument? {
        val query = Query(Criteria.where("sqlId").`is`(id))
        return mongoTemplate.findOne(query, CallListDocument::class.java)
    }
}

interface CallListMongoRepository : MongoRepository<CallListDocument, String>, CustomMongoCallListFunctions