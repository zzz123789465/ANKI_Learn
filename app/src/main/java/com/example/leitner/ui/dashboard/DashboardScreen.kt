package com.example.leitner.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DashboardScreen(onReview: () -> Unit, onAddCard: () -> Unit, viewModel: DashboardViewModel = hiltViewModel()) {
    val summaries by viewModel.summaries.collectAsStateWithLifecycle()
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("ANKI Learn", style = MaterialTheme.typography.headlineLarge)
            Text("用 Leitner Box，穩定累積記憶。", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onReview, modifier = Modifier.fillMaxWidth(), enabled = summaries.sumOf { it.dueCount } > 0) {
                Icon(Icons.Rounded.PlayArrow, null)
                Text("開始今日複習", Modifier.padding(start = 8.dp))
            }
            Button(onClick = onAddCard, modifier = Modifier.fillMaxWidth()) { Text("管理卡片") }
            LazyVerticalGrid(columns = GridCells.Adaptive(150.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(summaries) { summary ->
                    Card { Column(Modifier.padding(16.dp)) { Text(summary.box.label, style = MaterialTheme.typography.titleMedium); Text("${summary.totalCount} 張", style = MaterialTheme.typography.headlineSmall); Text("待複習 ${summary.dueCount} 張") } }
                }
            }
        }
    }
}
