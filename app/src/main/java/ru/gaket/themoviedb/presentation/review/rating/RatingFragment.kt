package ru.gaket.themoviedb.presentation.review.rating

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import ru.gaket.themoviedb.R
import ru.gaket.themoviedb.core.navigation.MovieDetailsScreen
import ru.gaket.themoviedb.core.navigation.Navigator
import ru.gaket.themoviedb.databinding.FragmentReviewRatingBinding
import ru.gaket.themoviedb.domain.review.Rating
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class RatingFragment : Fragment(R.layout.fragment_review_rating) {

    @Inject
    lateinit var navigator: Navigator

    private val binding: FragmentReviewRatingBinding
        get() = FragmentReviewRatingBinding.bind(requireView())

    private val viewModel: RatingViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO [Vlad] Add async work, validation, error handling, navigation
        with(binding) {
            btnSubmit.setOnClickListener {
                viewModel.submit(
                    Rating.mapToRating(rbRateMovie.rating.roundToInt())
                )
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.navigateBackEvent.collect {
                navigator.backTo(MovieDetailsScreen.TAG)
            }
        }

    }

}