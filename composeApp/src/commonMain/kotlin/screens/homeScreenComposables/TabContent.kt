package screens.homeScreenComposables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.Report
import io.ktor.client.*

@Composable
fun LocalTabContent(
    client: HttpClient,
    localReports: List<Report>,
    user: String?,
    onDeleteReport: (Report) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(localReports) { report ->
            Reports(client, report, user, onDeleteReport)
        }
    }
}

@Composable
fun StateTabContent(
    client: HttpClient,
    stateReports: List<Report>,
    user: String?,
    onDeleteReport: (Report) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(stateReports) { report ->
            Reports(client, report, user, onDeleteReport)
        }
    }
}

@Composable
fun NationalTabContent(
    client: HttpClient,
    nationalReports: List<Report>,
    user: String?,
    onDeleteReport: (Report) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(nationalReports) { report ->
            Reports(client, report, user, onDeleteReport)
        }
    }
}
