package pt.arbitros.arbnet.repositoryJdbi

// import com.daw.gomoku.repository.jdbi.mappers.PasswordValidationInfoMapper
// import com.daw.gomoku.repository.jdbi.mappers.TokenValidationInfoMapper
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import pt.arbitros.arbnet.repository.InstantMapper

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

//    registerColumnMapper(PasswordValidationInfoMapper())
//    registerColumnMapper(TokenValidationInfoMapper())
    registerColumnMapper(InstantMapper())

    return this
}
