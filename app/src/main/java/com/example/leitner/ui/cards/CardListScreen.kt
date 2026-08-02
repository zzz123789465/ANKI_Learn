package com.example.leitner.ui.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
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
fun CardListScreen(viewModel: CardListViewModel = hiltViewModel()) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = viewModel::addSampleCard) { Icon(Icons.Rounded.Add, "新增卡片") } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            Text("我的卡片", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(cards, key = { it.id }) { card ->
                    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(card.front, style = MaterialTheme.typography.titleMedium); Text(card.back, modifier = Modifier.padding(top = 6.dp)); Text("${card.box.label} · 下次 ${card.nextReviewDate}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp)) } }
                }
            }
        }
    }
}
