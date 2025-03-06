package components.FileReport

import components.classes.*
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import interfaces.ReportRepository
import okio.SYSTEM
import kotlin.random.Random
import kotlinx.serialization.json.Json.Default.decodeFromString
import kotlinx.serialization.json.Json.Default.encodeToString
import kotlinx.serialization.builtins.ListSerializer

// ATTENTION: This file is a WIP and will be updated when system is more thoroughly developed

// this is the report class for using a file system
class FileReportRepository : ReportRepository {
    private val fileSystem = FileSystem.SYSTEM

    private val filePath = "/home/benjaminv/fleet/test/Weather-Report/composeApp/src/commonMain/resources/reports.json".toPath()

    val rootPath = "/".toPath()
    val files = FileSystem.SYSTEM.list(rootPath)




    // grab all groups of reports
    override fun getGroups(): List<Group> {
        println("getGroups() called")
        println("Files $files")

        // make sure there is a file to grab

        if (!fileSystem.exists(filePath)) {
            println("File does not exist")
            return emptyList()
        }

        // grab information from the file
        val jsonContent = fileSystem.read(filePath) {
            readUtf8()
        }

        println("File content: $jsonContent")

        val groups = decodeFromString(ListSerializer(Group.serializer()), jsonContent)
        return groups
    }

    // grab all reports by group
    override fun getReports(group: String): List<Report> {
        println("getReports() called")
        // grab all data from the file
        val groups = getGroups()
        println("Here are the groups")
        println(groups)

        // return the group of reports requested if found, emptylist otherwise
        return groups.find { it.name == group }?.reports ?: emptyList()
    }

    override fun addReport(author: String, content: String, group: String, date: String) {
        println("addReport() called")
        // grab all groups of reports
        val groups = getGroups().toMutableList()

        // grab the specified group
        val reports = getReports(group).toMutableList()

        if (reports.isNotEmpty()) {
            // random report id
            val report = Report(Random.nextInt(10000, 99999), author, content, group, date)
            reports.add(report)

            // update the groups of reports with the new report
            val updatedGroups = groups.map { if (it.name == group) it.copy(reports = reports) else it }

            // update the json
            fileSystem.write(filePath) {
                writeUtf8(encodeToString(ListSerializer(Group.serializer()), updatedGroups))
            }

        } else {
            // if group is not found, throw an error
            throw IllegalArgumentException("Group '$group' not found")
        }



    }
}

