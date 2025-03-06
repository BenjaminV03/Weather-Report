// This will be the main homepage/dashboard that a user will see upon logging in
package screens

import components.classes.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(user: String?, onLogout: () -> Unit, reports: List<Report>) {
    println("Reports passed to HomeScreen: $reports")
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("Welcome, $user!")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onLogout) {
                Text("Logout")
                Spacer(Modifier.width(8.dp))
            }
        }
        items(reports) { report ->
            Reports(report)
        }
    }
}

@Composable
fun Reports(report: Report){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ){
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = report.author, style = MaterialTheme.typography.caption)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = report.content, style = MaterialTheme.typography.body1)
        }
    }
}