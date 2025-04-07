package components

import kotlinx.serialization.Serializable

// This is the group classification that each report belongs to
// This includes, local areas, states, the nation, etc.
// This also includes private groups in different areas
@Serializable
data class User(
    val id: Long = 0,
    val name: String = "",
    val email: String = "",
    val password: String = ""
)

// This is the report class itself. Right now this only allows text, will include images and videos later
@Serializable
data class Report(
    val id: Long = 0,
    val author: String = "",
    val groupName: String = "",
    val content: String = "",
    val createdDate: String = ""
)