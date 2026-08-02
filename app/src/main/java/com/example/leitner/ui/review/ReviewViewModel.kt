package com.example.leitner.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leitner.domain.model.Flashcard
import com.example.leitner.domain.repository.CardRepository
import com.example.leitner.domain.repository.ReviewAnswer
import com.example.leitner.domain.settings.StudySettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReviewState(
    val cards: List<Flashcard> = emptyList(),
    val index: Int = 0,
    val flipped: Boolean = false,
    val loading: Boolean = true,
    val dailyLimit: Int = 20,
    val completedToday: Int = 0
) {
    val current: Flashcard? get() = cards.getOrNull(index)
    val quotaReached: Boolean get() = completedToday >= dailyLimit
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repository: CardRepository,
    private val settings: StudySettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ReviewState())
    val state: StateFlow<ReviewState> = _state.asStateFlow()

    init { loadSession() }

    private fun loadSession() = viewModelScope.launch {
        val limit = settings.getDailyLimit()
        val completed = settings.getCompletedToday()
        val remaining = (limit - completed).coerceAtLeast(0)
        val dueCards = if (remaining > 0) repository.getDueCards().take(remaining) else emptyList()
        _state.value = ReviewState(cards = dueCards, loading = false, dailyLimit = limit, completedToday = completed)
    }

    fun flip() { _state.value = _state.value.copy(flipped = true) }

    fun answer(answer: ReviewAnswer) = viewModelScope.launch {
        val card = _state.value.current ?: return@launch
        repository.reviewCard(card.id, answer)
        settings.recordCompletedCard()
        _state.value = _state.value.copy(
            index = _state.value.index + 1,
            flipped = false,
            completedToday = _state.value.completedToday + 1
        )
    }
}
