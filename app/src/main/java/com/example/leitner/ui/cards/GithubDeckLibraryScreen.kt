package com.example.leitner.ui.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GithubDeckLibraryScreen(onBack: () -> Unit, viewModel: GithubDeckLibraryViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("GitHub Card Library", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
        Text("Search public GitHub Release .apkg assets. Check the author's license before use.", modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = query, onValueChange = viewModel::setQuery, modifier = Modifier.weight(1f), singleLine = true, label = { Text("Keyword") })
            Button(onClick = viewModel::search, enabled = query.isNotBlank()) { Text("Search") }
        }
        when (val current = state) {
            ImportState.Importing -> Text("Searching or downloading…", modifier = Modifier.padding(vertical = 12.dp))
            is ImportState.Success -> Text("Imported ${current.count} cards", modifier = Modifier.padding(vertical = 12.dp))
            is ImportState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 12.dp))
            ImportState.Idle -> Unit
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp)) {
            items(results, key = { it.downloadUrl }) { asset ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(asset.title, style = MaterialTheme.typography.titleMedium)
                        Text(asset.repository, modifier = Modifier.padding(top = 4.dp))
                        Text("Size: ${asset.sizeBytes / 1024 / 1024} MB", modifier = Modifier.padding(top = 4.dp))
                        Button(onClick = { viewModel.import(asset) }, modifier = Modifier.padding(top = 8.dp)) { Text("Download and import") }
                    }
                }
            }
        }
    }
}
