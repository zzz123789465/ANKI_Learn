package com.example.leitner.ui.cards

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CardListScreen(viewModel: CardListViewModel = hiltViewModel()) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.importDocument(it, context.contentResolver.getType(it)) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::addSampleCard) {
                Icon(Icons.Rounded.Add, contentDescription = "新增卡片")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("我的卡片", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = {
                    picker.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                }) {
                    Icon(Icons.Rounded.FileOpen, contentDescription = null)
                    Text("匯入", Modifier.padding(start = 6.dp))
                }
            }
            Text("支援 PDF 與 Word .docx；每行可用 Tab、冒號或等號分隔單字與解釋。", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
            when (val state = importState) {
                ImportState.Idle -> Unit
                ImportState.Importing -> Text("正在解析文件並建立卡片…", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 10.dp))
                is ImportState.Success -> Text("已匯入 ${state.count} 張卡片", color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 10.dp))
                is ImportState.Error -> Text("匯入失敗：${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 10.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(cards, key = { it.id }) { card ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(card.front, style = MaterialTheme.typography.titleMedium)
                            Text(card.back, modifier = Modifier.padding(top = 6.dp))
                            Text("${card.box.label} · 下次 ${card.nextReviewDate}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}
