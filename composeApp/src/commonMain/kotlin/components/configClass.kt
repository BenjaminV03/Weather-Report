package components

import kotlinx.serialization.Serializable

// This handles placing endpoints from the config file into a data class

@Serializable
data class Config(
    val baseurl: String,
    val authEndpoints: AuthEndpoints,
    val userEndpoints: UserEndpoints,
    val reportEndpoints: ReportEndpoints

)

@Serializable
data class AuthEndpoints(
    val login: String,
    val register: String,
    val verifyToken: String,
    val extractUserInfo: String
)

@Serializable
data class UserEndpoints(
    val findByEmail: String,
    val findByUsername: String,
    val updatePassword: String,
    val updateEmail: String
)

@Serializable
data class ReportEndpoints(
    val createReport: String,
    val fetchFile: String,
    val fetchFileNames: String,
    val getReportByGroup: String,
    val updateReport: String,
    val deleteReport: String,
    val getAllReports: String
)
