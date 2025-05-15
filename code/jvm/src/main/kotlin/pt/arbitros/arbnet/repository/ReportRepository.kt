package pt.arbitros.arbnet.repository

import org.springframework.data.mongodb.repository.MongoRepository
import pt.arbitros.arbnet.domain.Report

interface ReportRepository : MongoRepository<Report, String> {

}