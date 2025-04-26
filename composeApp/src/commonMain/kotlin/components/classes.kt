package components

import kotlinx.serialization.Serializable
import java.util.*

// This is the group classification that each report belongs to
// This includes, local areas, states, the nation, etc.
// This also includes private groups in different areas


@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class) val id: UUID,
    val username: String = "",
    val email: String = "",
    val password: String = ""
)

// Report class for each report
@Serializable
data class Report(
    @Serializable(with = UUIDSerializer::class) val id: UUID?,
    var author: String = "",
    var groupName: String = "",
    var content: String = "",
    // locations for reports dont need to ever be null since a report has to be created with a location
    var reportLat: Double,
    var reportLon: Double,
    val createdDate: String? = null // null to allows optional creation date
)