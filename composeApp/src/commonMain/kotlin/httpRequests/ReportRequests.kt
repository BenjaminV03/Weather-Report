package httpRequests

import components.Report

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.russhwolf.settings.Settings
import io.ktor.client.*

// these functions are used to access report data from the web servive via requsets handled by spring

// create a report
suspend fun postReport(client: HttpClient, report: Report){

    client.post("$baseurl/api/reports") {
        contentType(ContentType.Application.Json) // set type to Json
        setBody(report) // set body of Json as report object
        header("Authorization", "Bearer ${getAuthToken()}") // add auth token to header
    }.body<HttpResponse>().let { response ->
        when (response.status) {
            HttpStatusCode.OK -> println("Report created successfully") // Post was created successfully
            else -> {
                println("Failed to create report: ${response}")
                throw Exception(response.status.description)
            } // Post failed to create
        }
    }

}

// grab a report by id
suspend fun getReportById(client: HttpClient, id: Long): Report {

    val report: Report = client.get("$baseurl/api/reports/$id"){
        accept(ContentType.Application.Json)
        header("Authorization", "Bearer ${getAuthToken()}")
    }.body()
    return report
}
// grab a report by group
suspend fun getReportByGroup(client: HttpClient, groupName: String): List<Report> {

    try {
        val reports: List<Report> = client.get("$baseurl/api/reports/group/$groupName"){
            accept(ContentType.Application.Json)
            header("Authorization", "Bearer ${getAuthToken()}")
        }.body()

        println("Report retrieved successfully")
        return reports
    } catch (e: Exception) {
        println("Failed to retrieve report: ${e.message}")
        return emptyList()
    }

}
// update a report
suspend fun updateReport(client: HttpClient, report: Report) {
    client.put("$baseurl/api/reports/${report.id}") {
        contentType(ContentType.Application.Json) // set type to Json
        setBody(report) // set body of Json as report object
        header("Authorization", "Bearer ${getAuthToken()}") // add auth token to header
    }.body<HttpResponse>().let { response ->
        when (response.status) {
            HttpStatusCode.OK -> println("Report updated successfully")
            else -> println("Failed to update report: ${response.status}")
        }
    }
}

// delete a report
suspend fun deleteReport(client: HttpClient, id: Long?) {
    client.delete("$baseurl/api/reports/$id"){
        header("Authorization", "Bearer ${getAuthToken()}")
    }.body<HttpResponse>().let { response ->
        when (response.status) {
            HttpStatusCode.OK -> println("Report deleted successfully")
            else -> {
                println("Failed to delete report: ${response.status}")
                throw Exception(response.status.description)
            }
        }
    }
}

// grab all reports
suspend fun getAllReports(client: HttpClient): List<Report> {
    return client.get("$baseurl/api/reports"){
        accept(ContentType.Application.Json)
        header("Authorization", "Bearer ${getAuthToken()}")
    }.body()
}

//