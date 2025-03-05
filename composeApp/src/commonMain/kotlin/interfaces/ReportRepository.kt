package interfaces
import components.classes.*

// This will be used to switch between either accessing data from a file or from google cloud
// Its purpose it to create abstract methods that can be used for either kind of report data
interface ReportRepository {

    // grab all groups of reports
    fun getGroups(): List<Group>

    // grab all reports of a specific group
    fun getReports(group: Group): List<Report>

    // add a report to the database or file
    fun addReport(reportName: String, post: Report)
}