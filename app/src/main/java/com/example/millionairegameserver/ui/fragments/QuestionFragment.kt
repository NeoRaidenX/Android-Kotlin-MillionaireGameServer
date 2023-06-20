package com.example.millionairegameserver.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import com.example.millionairegameserver.Actions
import com.example.millionairegameserver.AnswersEnum
import com.example.millionairegameserver.App
import com.example.millionairegameserver.ui.viewmodel.QuestionViewModel
import com.example.millionairegameserver.R
import com.example.millionairegameserver.databinding.FragmentQuestionBinding
import com.example.millionairegameserver.datamodel.LifelineModel
import com.example.millionairegameserver.datamodel.QuestionModel
import com.example.millionairegameserver.ui.viewmodel.CurrentQuestionUiState
import com.example.millionairegameserver.ui.viewmodel.LifelinesUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch

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

    private val actionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive: ${intent.action}")
            when(intent.action) {
                Actions.NAVIGATE_TABLE -> {
                    val action = QuestionFragmentDirections.actionQuestionFragmentToTableFragment()
                    findNavController().navigate(action)
                }
                Actions.NAVIGATE_REWARD -> {
                    val action = QuestionFragmentDirections.actionQuestionFragmentToRewardFragment()
                    findNavController().navigate(action)
                }
                Actions.NAVIGATE_CHART -> {
                    val action = QuestionFragmentDirections.actionQuestionFragmentToChartFragment()
                    findNavController().navigate(action)
                }
                Actions.NAVIGATE_CLOCK -> {
                    val action = QuestionFragmentDirections.actionQuestionFragmentToClockFragment()
                    findNavController().navigate(action)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        viewModel = activity?.run {
            ViewModelProvider(this).get(QuestionViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        findNavController().addOnDestinationChangedListener { controller, destination, arguments ->
            Log.d(TAG, "destination changed: ${destination.label}")
            when(destination.label) {
                "fragment_question" -> {
                    Log.d(TAG, "fragment_question: ")
                }
                else -> {
                    viewModel.sendResetUi()
                    unregisterReceiver()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, "onCreateView: ")

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { currentQuestion ->
                        Log.d(TAG, "collect onCreateView: ${currentQuestion.javaClass.name}")
                        when (currentQuestion) {
                            is CurrentQuestionUiState.Error -> showError(currentQuestion.e)
                            is CurrentQuestionUiState.Success -> {
                                updateQuestionView(currentQuestion.currentQuestion)
                            }
                            is CurrentQuestionUiState.ShowQuestion -> showQuestion()
                            is CurrentQuestionUiState.ShowOption -> showOption(AnswersEnum.values()[currentQuestion.position])
                            is CurrentQuestionUiState.MarkAnswer -> markAnswer(AnswersEnum.values()[currentQuestion.position])
                            is CurrentQuestionUiState.CorrectAnswer -> correctAnswer(AnswersEnum.values()[currentQuestionModel.correct])
                            is CurrentQuestionUiState.ResetQuestionUi -> resetUiState()
                            is CurrentQuestionUiState.ShowFifty -> showFifty()
                        }
                    }
                }

                launch {
                    viewModel.lifelinesState.collect { lifelines ->
                        when(lifelines) {
                            is LifelinesUiState.Error -> {}
                            is LifelinesUiState.Success -> updateLifelines(lifelines.lifeline)
                        }
                    }
                }
            }
        }

        return binding.root

    }

    private fun showFifty() {
        //TODO check action error. All options go blank
        if(!currentQuestionModel.isOptA) binding.ansA.text = ""
        if(!currentQuestionModel.isOptB) binding.ansB.text = ""
        if(!currentQuestionModel.isOptC) binding.ansC.text = ""
        if(!currentQuestionModel.isOptD) binding.ansD.text = ""
    }

    private fun updateLifelines(lifeline: LifelineModel) {
        binding.togglePhone.visibility = if (lifeline.lifelinePhone) View.VISIBLE else View.GONE
        binding.toggle50.visibility = if (lifeline.lifelineFifty) View.VISIBLE else View.GONE
        binding.toggleGroup.visibility = if (lifeline.lifelineGroup) View.VISIBLE else View.GONE
        binding.toggleChart.visibility = if (lifeline.lifelineChart) View.VISIBLE else View.GONE
    }

    private fun showQuestion() {
        Log.d(TAG, "showQuestion: ")
        binding.questionTitle.text = currentQuestionModel.question
    }

    /*private fun changeNextQuestion() {
        lifecycleScope.launch {
            viewModel.changeNextQuestion()
        }
    }*/

    private fun resetUiState() {
        binding.questionTitle.text = ""
        binding.ansA.text = ""
        binding.ansB.text = ""
        binding.ansC.text = ""
        binding.ansD.text = ""
        binding.ansABg.setImageResource(R.drawable.question_title)
        binding.ansBBg.setImageResource(R.drawable.question_title)
        binding.ansCBg.setImageResource(R.drawable.question_title)
        binding.ansDBg.setImageResource(R.drawable.question_title)
    }


    private fun correctAnswer(position: AnswersEnum) {
        Log.d(TAG, "correctAnswer: ")
        when(position) {
            AnswersEnum.ANSWER_A -> binding.ansABg.setImageResource(R.drawable.answer_correct)
            AnswersEnum.ANSWER_B -> binding.ansBBg.setImageResource(R.drawable.answer_correct)
            AnswersEnum.ANSWER_C -> binding.ansCBg.setImageResource(R.drawable.answer_correct)
            AnswersEnum.ANSWER_D -> binding.ansDBg.setImageResource(R.drawable.answer_correct)
        }
        updateLastAnswered()
    }

    private fun updateLastAnswered() {
        viewModel.changeNextQuestion()
    }

    private fun markAnswer(position: AnswersEnum) {
        Log.d(TAG, "markAnswer: ")
        when(position) {
            AnswersEnum.ANSWER_A -> {
                binding.ansABg.setImageResource(R.drawable.answer_marked)
                binding.ansBBg.setImageResource(R.drawable.question_title)
                binding.ansCBg.setImageResource(R.drawable.question_title)
                binding.ansDBg.setImageResource(R.drawable.question_title)
            }
            AnswersEnum.ANSWER_B -> {
                binding.ansABg.setImageResource(R.drawable.question_title)
                binding.ansBBg.setImageResource(R.drawable.answer_marked)
                binding.ansCBg.setImageResource(R.drawable.question_title)
                binding.ansDBg.setImageResource(R.drawable.question_title)
            }
            AnswersEnum.ANSWER_C -> {
                binding.ansABg.setImageResource(R.drawable.question_title)
                binding.ansBBg.setImageResource(R.drawable.question_title)
                binding.ansCBg.setImageResource(R.drawable.answer_marked)
                binding.ansDBg.setImageResource(R.drawable.question_title)
            }
            AnswersEnum.ANSWER_D -> {
                binding.ansABg.setImageResource(R.drawable.question_title)
                binding.ansBBg.setImageResource(R.drawable.question_title)
                binding.ansCBg.setImageResource(R.drawable.question_title)
                binding.ansDBg.setImageResource(R.drawable.answer_marked)
            }
        }
    }

    private fun showOption(position: AnswersEnum) {
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
        Toast.makeText(requireContext(), currentQuestion.uid.toString(), Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        registerReceiver()
        resetUiState()
        viewModel.sendLoadCurrentQuestion()
        /*lifecycleScope.launch {
            delay(1000)
            viewModel.getCurrentQuestion()
        }*/
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        unregisterReceiver()
    }

    private fun registerReceiver() {
        Log.d(TAG, "registerReceiver: ")
        App.applicationContext().registerReceiver(actionReceiver, getLoginIntentFilter())
    }

    private fun unregisterReceiver() {
        Log.d(TAG, "unregisterReceiver: ")
        App.applicationContext().unregisterReceiver(actionReceiver)
    }
    private fun getLoginIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Actions.NAVIGATE_REWARD)
        intentFilter.addAction(Actions.NAVIGATE_TABLE)
        intentFilter.addAction(Actions.NAVIGATE_CHART)
        intentFilter.addAction(Actions.NAVIGATE_CLOCK)
        return intentFilter
    }

}