package com.example.leitner.ui.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leitner.data.importer.GithubDeckAsset
import com.example.leitner.data.importer.GithubDeckImporter
import com.example.leitner.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class GithubDeckLibraryViewModel @Inject constructor(
    private val importer: GithubDeckImporter,
    private val repository: CardRepository
) : ViewModel() {
    private val _query = MutableStateFlow("TOEIC")
    val query = _query.asStateFlow()
    private val _results = MutableStateFlow<List<GithubDeckAsset>>(emptyList())
    val results = _results.asStateFlow()
    private val _state = MutableStateFlow<ImportState>(ImportState.Idle)
    val state = _state.asStateFlow()

    fun setQuery(value: String) { _query.value = value }

    fun search() = viewModelScope.launch {
        _state.value = ImportState.Importing
        runCatching { importer.search(_query.value.trim()) }
            .onSuccess { _results.value = it; _state.value = ImportState.Idle }
            .onFailure { _state.value = ImportState.Error(it.message ?: "搜尋失敗") }
    }

    fun import(asset: GithubDeckAsset) = viewModelScope.launch {
        _state.value = ImportState.Importing
        runCatching {
            val cards = importer.import(asset)
            require(cards.isNotEmpty()) { "這個卡組沒有可匯入的雙欄位卡片" }
            repository.addCards(cards)
        }.onSuccess { _state.value = ImportState.Success(it) }
            .onFailure { _state.value = ImportState.Error(it.message ?: "匯入失敗") }
    }
}
