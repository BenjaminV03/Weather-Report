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
    val createdDate: String? = null // null to allows optional creation date
)