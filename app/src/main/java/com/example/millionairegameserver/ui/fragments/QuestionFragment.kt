package com.example.millionairegameserver.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import com.example.millionairegameserver.AnswersEnum
import com.example.millionairegameserver.ui.viewmodel.QuestionViewModel
import com.example.millionairegameserver.R
import com.example.millionairegameserver.databinding.FragmentQuestionBinding
import com.example.millionairegameserver.datamodel.QuestionModel
import com.example.millionairegameserver.ui.viewmodel.CurrentQuestionUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.log

@AndroidEntryPoint
@UnstableApi
class QuestionFragment : Fragment() {

    companion object {
        private const val TAG = "QuestionFragment"
        fun newInstance() = QuestionFragment()
    }

    private lateinit var viewModel: QuestionViewModel

    private lateinit var binding: FragmentQuestionBinding
    private lateinit var playerview: PlayerView
    private lateinit var exoPlayer: ExoPlayer

    private lateinit var currentQuestionModel: QuestionModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this).get(QuestionViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuestionBinding.inflate(inflater, container, false)

        playerview = binding.playerview

        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        playerview.player = exoPlayer

        val uri = RawResourceDataSource.buildRawResourceUri(R.raw.bg_loop)
        val mediaItem = MediaItem.fromUri(uri)
        val mediaSource = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(requireContext()))
            .createMediaSource(mediaItem)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.prepare()
        exoPlayer.play()

        exoPlayer.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log.d(TAG, "onPlaybackStateChanged: $playbackState")
            }
        })

        lifecycleScope.launch {
            viewModel.uiState.collect { currentQuestion ->
                Log.d(TAG, "collect onCreateView: ${currentQuestion.javaClass.name}")
                when (currentQuestion) {
                    is CurrentQuestionUiState.Error -> showError(currentQuestion.e)
                    is CurrentQuestionUiState.Success -> {
                        updateQuestionView(currentQuestion.currentQuestion)
                    }
                    is CurrentQuestionUiState.ShowAnswer -> showAnswer(AnswersEnum.values()[currentQuestion.position])
                    is CurrentQuestionUiState.MarkAnswer -> markAnswer(AnswersEnum.values()[currentQuestion.position])
                    is CurrentQuestionUiState.CorrectAnswer -> correctAnswer(AnswersEnum.values()[currentQuestion.position])
                    is CurrentQuestionUiState.Navigate -> navigateToReward()
                }
            }
        }

        return binding.root

    }

    private fun correctAnswer(position: AnswersEnum) {
        Log.d(TAG, "correctAnswer: ")
        when(position) {
            AnswersEnum.ANSWER_A -> binding.ansABg.setImageResource(R.drawable.answer_correct)
            AnswersEnum.ANSWER_B -> binding.ansBBg.setImageResource(R.drawable.answer_correct)
            AnswersEnum.ANSWER_C -> binding.ansCBg.setImageResource(R.drawable.answer_correct)
            AnswersEnum.ANSWER_D -> binding.ansDBg.setImageResource(R.drawable.answer_correct)
        }
    }

    private fun markAnswer(position: AnswersEnum) {
        Log.d(TAG, "markAnswer: ")
        when(position) {
            AnswersEnum.ANSWER_A -> binding.ansABg.setImageResource(R.drawable.answer_marked)
            AnswersEnum.ANSWER_B -> binding.ansBBg.setImageResource(R.drawable.answer_marked)
            AnswersEnum.ANSWER_C -> binding.ansCBg.setImageResource(R.drawable.answer_marked)
            AnswersEnum.ANSWER_D -> binding.ansDBg.setImageResource(R.drawable.answer_marked)
        }
    }

    private fun showAnswer(position: AnswersEnum) {
        Log.d(TAG, "showAnswer: ")
        when(position) {
            AnswersEnum.ANSWER_A -> binding.ansA.text = currentQuestionModel.optionA
            AnswersEnum.ANSWER_B -> binding.ansB.text = currentQuestionModel.optionB
            AnswersEnum.ANSWER_C -> binding.ansC.text = currentQuestionModel.optionC
            AnswersEnum.ANSWER_D -> binding.ansD.text = currentQuestionModel.optionD
        }
    }

    private fun showError(e: Throwable) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }

    private fun updateQuestionView(currentQuestion: QuestionModel) {
        Log.d(TAG, "updateQuestionView: ")
        currentQuestionModel = currentQuestion
        binding.questionTitle.text = currentQuestion.question
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            delay(3000)
            viewModel.getCurrentQuestion()
        }
    }

    private fun navigateToReward() {
        val action = QuestionFragmentDirections.actionQuestionFragmentToRewardFragment()
        findNavController().navigate(action)
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.stop()
        exoPlayer.release()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}