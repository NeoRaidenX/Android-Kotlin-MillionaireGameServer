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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.millionairegameserver.Actions
import com.example.millionairegameserver.App
import com.example.millionairegameserver.R
import com.example.millionairegameserver.databinding.FragmentQuestionBinding
import com.example.millionairegameserver.databinding.FragmentTableBinding
import com.example.millionairegameserver.ui.RewardTableAdapter
import com.example.millionairegameserver.ui.viewmodel.CurrentRewardUiState
import com.example.millionairegameserver.ui.viewmodel.QuestionViewModel
import com.example.millionairegameserver.ui.viewmodel.TableViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@UnstableApi class TableFragment : Fragment() {

    companion object {
        fun newInstance() = TableFragment()
        private const val TAG = "TableFragment"
    }

    private lateinit var viewModel: TableViewModel
    private lateinit var binding: FragmentTableBinding

    private lateinit var playerview: PlayerView
    private lateinit var exoPlayer: ExoPlayer

    private val actionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive: ${intent.action}")
            when(intent.action) {
                Actions.NAVIGATE_UP -> {
                    findNavController().navigateUp()
                }
                else -> {
                    Log.d(TAG, "onReceive: ")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this).get(TableViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentTableBinding.inflate(inflater, container, false)
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

        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        val adapter = RewardTableAdapter(requireContext())
        binding.recyclerview.adapter = adapter
        binding.recyclerview.setHasFixedSize(true)

        lifecycleScope.launch {
            viewModel.uiState.collect { currentReward ->
                when (currentReward) {
                    is CurrentRewardUiState.Error -> showError(currentReward.e)
                    is CurrentRewardUiState.Success -> {
                        adapter.updateCurrentPosition(currentReward.position)
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerReceiver()
        lifecycleScope.launch {
            viewModel.getCurrentReward()
        }
    }

    private fun getLoginIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Actions.NAVIGATE_UP)
        return intentFilter
    }

    private fun showError(e: Throwable) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
        exoPlayer?.stop()
        exoPlayer.release()
        unregisterReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        
    }

    private fun registerReceiver() {
        Log.d(TAG, "registerReceiver: ")
        App.applicationContext().registerReceiver(actionReceiver, getLoginIntentFilter())
    }

    private fun unregisterReceiver() {
        Log.d(TAG, "unregisterReceiver: ")
        App.applicationContext().unregisterReceiver(actionReceiver)
    }
}