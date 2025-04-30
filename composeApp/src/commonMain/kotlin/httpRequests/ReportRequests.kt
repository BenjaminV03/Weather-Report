package httpRequests

import components.Report
import screens.homeScreenComposables.utilities.getMimeType

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

val reportEndpoints = config.reportEndpoints

// these functions are used to access report data from the web servive via requsets handled by spring

// create a report
suspend fun postReport(client: HttpClient, report: Report, files: List<File>): HttpStatusCode {
    val url = baseurl + reportEndpoints.createReport
    client.submitFormWithBinaryData(
        url = url,
        formData = formData {
            // Add the Report object as JSON
            append("report", Json.encodeToString(report), Headers.build {
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            })

            // Add each file to the form data only if the list is not empty
            if (files.isNotEmpty()) {
                files.forEach { file ->
                    val sanitizedFileName = file.name.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
                    val mimeType = file.getMimeType()
                    append(
                        "files",
                        file.readBytes(),
                        Headers.build {
                            // Explicitly construct the Content-Disposition header
                            append(HttpHeaders.ContentDisposition, "filename=\"$sanitizedFileName\"")
                            append(HttpHeaders.ContentType, mimeType)
                        }
                    )
                }
            }
        }
    ) {
        header("Authorization", "Bearer ${getAuthToken()}") // Add auth token to header
    }.let { response ->
        when (response.status) {
            HttpStatusCode.OK -> {
                println("Report created successfully")
                return response.status
            }
            else -> {
                println("Failed to create report: ${response.status}")
                return response.status
            }
        }
    }
}

// grab a file from the server
suspend fun fetchFile(client: HttpClient, reportId: UUID, fileName: String): ByteArray {
    val url = baseurl + reportEndpoints.fetchFile
        .replace("{reportId}", reportId.toString())
        .replace("{fileName}", fileName)
    return client.get(url) {
        header("Authorization", "Bearer ${getAuthToken()}")
    }.body()
}

suspend fun fetchFileNames(client: HttpClient, reportId: UUID): List<String> {
    val url = baseurl + reportEndpoints.fetchFileNames.replace("{reportId}", reportId.toString())
    return client.get(url){
        header("Authorization", "Bearer ${getAuthToken()}")
    }.body()
}

// grab a report by group
suspend fun getReportsByGroup(client: HttpClient, groupName: String): List<Report> {
    val url = baseurl + reportEndpoints.getReportsByGroup.replace("{groupName}",
        groupName.lowercase(Locale.getDefault())
    )
    try {
        val reports: List<Report> = client.get(url){
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

suspend fun getReportsByGroups(client: HttpClient, groupNames: List<String>): List<Report> {
    val url = baseurl + reportEndpoints.getReportsByGroups
    return try {
        val reports: List<Report> = client.get(url) {
            accept(ContentType.Application.Json)
            url {
                parameters.appendAll("groupNames", groupNames) // Add group names as query parameters
            }
            header("Authorization", "Bearer ${getAuthToken()}") // add auth token to header
        }.body()
        reports
    } catch (e: Exception) {
        println("Failed to retrieve reports: ${e.message}")
        emptyList()
    }
}

// update a report
suspend fun updateReport(client: HttpClient, report: Report) {
    val url = baseurl + reportEndpoints.updateReport.replace("{reportId}", report.id.toString())
    client.put(url) {
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
suspend fun deleteReport(client: HttpClient, id: UUID?) {
    val url = baseurl + reportEndpoints.deleteReport.replace("{reportId}", id.toString())
    client.delete(url){
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
    val url = baseurl + reportEndpoints.getAllReports
    return client.get(url){
        accept(ContentType.Application.Json)
        header("Authorization", "Bearer ${getAuthToken()}")
    }.body()
}
