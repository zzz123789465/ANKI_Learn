package com.example.leitner.ui.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leitner.domain.model.Flashcard
import com.example.leitner.domain.repository.CardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CardListViewModel @Inject constructor(private val repository: CardRepository) : ViewModel() {
    val cards: StateFlow<List<Flashcard>> = repository.observeAllCards().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun addSampleCard() = viewModelScope.launch { repository.addCard("新問題", "新答案") }
}
