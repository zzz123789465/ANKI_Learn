package com.example.leitner.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leitner.domain.model.Flashcard
import com.example.leitner.domain.repository.CardRepository
import com.example.leitner.domain.repository.ReviewAnswer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewState(val cards: List<Flashcard> = emptyList(), val index: Int = 0, val flipped: Boolean = false, val loading: Boolean = true) {
    val current: Flashcard? get() = cards.getOrNull(index)
}

@HiltViewModel
class ReviewViewModel @Inject constructor(private val repository: CardRepository) : ViewModel() {
    private val _state = MutableStateFlow(ReviewState())
    val state: StateFlow<ReviewState> = _state.asStateFlow()

    init { viewModelScope.launch { _state.value = ReviewState(repository.getDueCards(), loading = false) } }
    fun flip() { _state.value = _state.value.copy(flipped = true) }
    fun answer(answer: ReviewAnswer) = viewModelScope.launch {
        val card = _state.value.current ?: return@launch
        repository.reviewCard(card.id, answer)
        _state.value = _state.value.copy(index = _state.value.index + 1, flipped = false)
    }
}
