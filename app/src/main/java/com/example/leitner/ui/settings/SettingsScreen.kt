package com.example.leitner.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("設定", style = MaterialTheme.typography.headlineMedium)
        Text("每日抽卡上限", style = MaterialTheme.typography.titleMedium)
        Text("每完成一張複習卡就會計入今日額度，隔天自動重新計算。")
        OutlinedTextField(
            value = state.dailyLimitInput,
            onValueChange = viewModel::updateDailyLimit,
            label = { Text("每天張數") },
            supportingText = { Text(state.error ?: "可設定 1～500 張") },
            isError = state.error != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth()) { Text("儲存設定") }
        if (state.saved) Text("已儲存，每日上限為 ${state.dailyLimitInput} 張。", color = MaterialTheme.colorScheme.primary)
    }
}
