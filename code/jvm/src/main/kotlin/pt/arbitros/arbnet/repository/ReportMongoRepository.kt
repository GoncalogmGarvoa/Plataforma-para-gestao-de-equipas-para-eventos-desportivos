package pt.arbitros.arbnet.repository

import org.springframework.data.mongodb.repository.MongoRepository
import pt.arbitros.arbnet.domain.Report

// This interface extends the MongoRepository interface provided by Spring Data MongoDB.
// It allows us to perform CRUD operations on the Report entity without writing any boilerplate code.
// The Report entity is expected to have a String type ID.
interface ReportMongoRepository : MongoRepository<Report, String>