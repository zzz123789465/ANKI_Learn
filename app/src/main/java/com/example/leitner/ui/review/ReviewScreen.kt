package com.example.leitner.ui.review

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.leitner.domain.repository.ReviewAnswer

@Composable
fun ReviewScreen(onClose: () -> Unit, viewModel: ReviewViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("今日複習", style = MaterialTheme.typography.headlineMedium)
        if (!state.loading) {
            Text("今日進度：${state.completedToday} / ${state.dailyLimit}", style = MaterialTheme.typography.labelLarge)
        }
        when {
            state.loading -> Text("載入中…")
            state.quotaReached -> {
                Text("今天的抽卡額度已用完。", style = MaterialTheme.typography.titleLarge)
                Button(onClick = onClose) { Text("返回首頁") }
            }
            state.current == null -> {
                Text("今天沒有待複習卡片。", style = MaterialTheme.typography.titleLarge)
                Button(onClick = onClose) { Text("返回首頁") }
            }
            else -> {
                Text("本次 ${state.index + 1} / ${state.cards.size}")
                val card = state.current!!
                FlipCard(card.front, card.back, state.flipped, viewModel::flip, Modifier.fillMaxWidth().weight(1f))
                if (state.flipped) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { viewModel.answer(ReviewAnswer.INCORRECT) }, modifier = Modifier.weight(1f)) { Text("答錯 · Box 1") }
                    Button(onClick = { viewModel.answer(ReviewAnswer.CORRECT) }, modifier = Modifier.weight(1f)) { Text("答對 · 晉級") }
                } else Button(onClick = viewModel::flip, modifier = Modifier.fillMaxWidth()) { Text("顯示答案") }
            }
        }
    }
}

@Composable
private fun FlipCard(front: String, back: String, flipped: Boolean, onFlip: () -> Unit, modifier: Modifier) {
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, label = "cardRotation")
    Card(modifier.heightIn(min = 280.dp).graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }.clickable(onClick = onFlip)) {
        Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.graphicsLayer { if (flipped) rotationY = 180f }) {
                Text(if (flipped) "答案" else "問題", color = MaterialTheme.colorScheme.primary)
                Text(if (flipped) back else front, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 20.dp))
            }
        }
    }
}
