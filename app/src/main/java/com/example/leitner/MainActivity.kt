package com.example.leitner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.example.leitner.ui.navigation.LeitnerNavHost
import com.example.leitner.ui.theme.AnkiLearnTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnkiLearnTheme {
                LeitnerNavHost()
            }
        }
    }
}
