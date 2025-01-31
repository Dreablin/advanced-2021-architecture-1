package ru.gaket.themoviedb.presentation.review.rating

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import ru.gaket.themoviedb.data.auth.AuthRepository
import ru.gaket.themoviedb.data.movies.MoviesRepository
import ru.gaket.themoviedb.domain.review.models.Rating
import ru.gaket.themoviedb.domain.review.repository.ReviewRepository
import javax.inject.Inject

@HiltViewModel
class RatingViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val moviesRepository: MoviesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _reviewEvent = MutableSharedFlow<ReviewEvent>()
    val reviewEvent: LiveData<ReviewEvent>
        get() = _reviewEvent
            .asLiveData(viewModelScope.coroutineContext)

    private val _reviewState = MutableLiveData<ReviewState>()
    val reviewState: LiveData<ReviewState> get() = _reviewState

    fun submit(ratingNumber: Int) {
        viewModelScope.launch {
            val rating = Rating.mapToRating(ratingNumber)
            if (rating == null) {
                _reviewEvent.emit(ReviewEvent.ERROR_ZERO_RATING)
            } else {
                reviewRepository.setRating(rating)
                submitReview()
            }
        }
    }

    //TODO [Vlad] Maybe move to useCase
    private suspend fun submitReview() {
        _reviewState.value = ReviewState.LOADING

        val currentUser = authRepository.currentUser
        if (currentUser == null) {
            _reviewEvent.emit(ReviewEvent.ERROR_USER_NOT_SIGNED)
        } else {
            try {
                val review = reviewRepository.buildReview()
                reviewRepository.clearState()
                moviesRepository.addReview(review, currentUser.id, currentUser.email)

                _reviewEvent.emit(ReviewEvent.SUCCESS)
            } catch (e: IllegalStateException) {
                _reviewEvent.emit(ReviewEvent.ERROR_UNKNOWN)
            }
        }

        _reviewState.value = ReviewState.IDLE
    }

    enum class ReviewState {
        IDLE,
        LOADING
    }

    enum class ReviewEvent {
        ERROR_ZERO_RATING,
        ERROR_UNKNOWN,
        ERROR_USER_NOT_SIGNED,
        SUCCESS
    }
}
