package com.example.millionairegameserver.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
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
import com.example.millionairegameserver.SongTypeEnum
import com.example.millionairegameserver.databinding.FragmentQuestionBinding
import com.example.millionairegameserver.datamodel.LifelineModel
import com.example.millionairegameserver.datamodel.QuestionModel
import com.example.millionairegameserver.ui.viewmodel.CurrentQuestionUiState
import com.example.millionairegameserver.ui.viewmodel.LifelinesUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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

    private lateinit var mediaPlayer: MediaPlayer

    private var markedOptionPosition: Int = 0

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
                    val action = QuestionFragmentDirections.actionQuestionFragmentToPersonFragment()
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
                    //viewModel.sendResetUi()
                    //unregisterReceiver()
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState.collect { currentQuestion ->
                        Log.d(TAG, "collect : ${currentQuestion.javaClass.name}")
                        when (currentQuestion) {
                            is CurrentQuestionUiState.Error -> showError(currentQuestion.e)
                            is CurrentQuestionUiState.Success -> {
                                updateQuestionView(currentQuestion.currentQuestion)
                                Log.d(TAG, "Success: ${currentQuestion.currentQuestion.uid}")
                            }
                            is CurrentQuestionUiState.ShowQuestion -> playIntroQuestion()
                            is CurrentQuestionUiState.ShowOption -> showOption(AnswersEnum.values()[currentQuestion.position])
                            is CurrentQuestionUiState.MarkAnswer -> markAnswer(AnswersEnum.values()[currentQuestion.position])
                            is CurrentQuestionUiState.CorrectAnswer -> correctAnswer(AnswersEnum.values()[currentQuestionModel.correct])
                            is CurrentQuestionUiState.ResetQuestionUi -> {}
                            is CurrentQuestionUiState.ShowFifty -> showFifty()
                            is CurrentQuestionUiState.UpdateSuccess -> {
                                Toast.makeText(requireContext(), "${currentQuestion.position + 1}", Toast.LENGTH_SHORT).show()
                                viewModel.sendLoadCurrentQuestion()

                            }
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

    private fun loadBackground() {
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
    }

    private fun showFifty() {
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
        Log.d(TAG, "resetUiState: ")
        loadBackground()
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
        checkIfCorrectAnswer(position.ordinal)
    }

    private fun checkIfCorrectAnswer(correctAnswer: Int) {
        if (correctAnswer == markedOptionPosition) {
            playCorrectSound()
        } else {
            playIncorrectSound()
        }
    }

    private fun playCorrectSound() {
        playAnswerSound(SongTypeEnum.CORRECT_ANSWER)
    }

    private fun playIncorrectSound() {
        playAnswerSound(SongTypeEnum.INCORRECT_ANSWER)
    }

    private fun updateLastAnswered() {
        viewModel.changeNextQuestion()
    }

    private fun markAnswer(position: AnswersEnum) {
        Log.d(TAG, "markAnswer: ")
        markedOptionPosition = position.ordinal
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
        playAnswerSound(SongTypeEnum.MARKED_ANSWER)
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
        //selectSongAndPlay()
        //Toast.makeText(requireContext(), currentQuestion.uid.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun playAnswerSound(answerType: SongTypeEnum) {

        mediaPlayer.pause()
        mediaPlayer.isLooping = false


        when (answerType) {
            SongTypeEnum.CORRECT_ANSWER -> {
                mediaPlayer = when(currentQuestionModel.uid + 1) {
                    in 16..19 -> {
                        MediaPlayer.create(requireContext(), R.raw.correct_hard_ans)
                    }
                    else -> {
                        MediaPlayer.create(requireContext(), R.raw.correct_answer)
                    }
                }
            }
            SongTypeEnum.MARKED_ANSWER -> {
                mediaPlayer = when (currentQuestionModel.uid + 1) {
                    in 6..10 -> {
                        MediaPlayer.create(requireContext(), R.raw.marked_ans_1)
                    }
                    in 11..15 -> {
                        MediaPlayer.create(requireContext(), R.raw.marked_ans_2)
                    }
                    in 16..20 -> {
                        MediaPlayer.create(requireContext(), R.raw.marked_ans_3)
                    }
                    else -> {
                        MediaPlayer.create(requireContext(), R.raw.marked_ans_1)
                    }
                }
            }
            SongTypeEnum.INCORRECT_ANSWER -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.incorrect_answer)
            }
            SongTypeEnum.LIFELINE_50 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.lifeline_50)
            }
        }
        mediaPlayer.start()
    }

    private fun playIntroQuestion() {
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.intro_question)
        mediaPlayer.isLooping = false
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            Log.d(TAG, "OnCompletionListener: ")
            selectSongAndPlay()
            showQuestion()
        }
    }

    private fun selectSongAndPlay() {
        if (mediaPlayer.isPlaying) mediaPlayer.stop()
        when (currentQuestionModel.uid + 1) {
            in 1..5 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.question_1_5)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
            6 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.question_6)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
            7 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.question_7)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
            8 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.question_8)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
            9 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.question_9)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
            10 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.question_10)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
            in 11..13 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.question_11_12_13)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
            in 14..15 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.question_14_15)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
            16 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.question_16)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
            17 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.question_17)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
            in 18..20 -> {
                mediaPlayer = MediaPlayer.create(requireContext(), R.raw.question_18_19_20)
                mediaPlayer.isLooping = true
                mediaPlayer.start()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ")
        registerReceiver()

        viewModel.sendLoadCurrentQuestion()

        /*lifecycleScope.launch {
            delay(1000)
            viewModel.getCurrentQuestion()
        }*/
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        resetUiState()
    }

    override fun onPause() {
        super.onPause()
        viewModel.sendResetUi()
        try {
            mediaPlayer.stop()
            mediaPlayer.release()
        } catch (e: Exception) {
            Log.d(TAG, "onPause: $e")
        }
        exoPlayer.stop()
        exoPlayer.release()
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