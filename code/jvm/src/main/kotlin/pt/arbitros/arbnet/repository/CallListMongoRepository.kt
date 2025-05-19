package pt.arbitros.arbnet.repository

import org.springframework.data.mongodb.repository.MongoRepository
import pt.arbitros.arbnet.domain.CallList

interface CallListMongoRepository : MongoRepository<CallList, String>