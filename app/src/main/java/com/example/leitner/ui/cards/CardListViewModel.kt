package com.example.leitner.ui.cards

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leitner.data.importer.DocumentImporter
import com.example.leitner.domain.model.Flashcard
import com.example.leitner.domain.repository.CardDraft
import com.example.leitner.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ImportState {
    data object Idle : ImportState
    data object Importing : ImportState
    data class Success(val count: Int) : ImportState
    data class Error(val message: String) : ImportState
}

@HiltViewModel
class CardListViewModel @Inject constructor(
    private val repository: CardRepository,
    private val documentImporter: DocumentImporter
) : ViewModel() {
    val cards: StateFlow<List<Flashcard>> = repository.observeAllCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun addSampleCard() = viewModelScope.launch { repository.addCard("新問題", "新答案") }

    fun importDocument(uri: Uri, mimeType: String?) = viewModelScope.launch {
        _importState.value = ImportState.Importing
        runCatching {
            val drafts = documentImporter.import(uri, mimeType)
            require(drafts.isNotEmpty()) { "文件中沒有找到可匯入的單字配對" }
            repository.addCards(drafts)
        }.onSuccess { count ->
            _importState.value = ImportState.Success(count)
        }.onFailure { error ->
            _importState.value = ImportState.Error(error.message ?: "匯入失敗")
        }
    }

    fun clearImportState() {
        _importState.value = ImportState.Idle
    }
}
