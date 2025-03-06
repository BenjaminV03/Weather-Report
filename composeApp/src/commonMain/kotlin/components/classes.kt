package components.classes

import kotlinx.serialization.Serializable

// This is the group classification that each report belongs to
// This includes, local areas, states, the nation, etc.
// This also includes private groups in different areas
@Serializable
data class Group(
    val name: String,
    val reports: MutableList<Report> = mutableListOf()
)

// This is the report class itself. Right now this only allows text, will include images and videos later
@Serializable
data class Report(
    val id: Int,
    val author: String,
    val group: String,
    val content: String,
    val timestamp: String
)