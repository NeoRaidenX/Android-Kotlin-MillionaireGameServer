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
import com.example.millionairegameserver.R
import com.example.millionairegameserver.databinding.FragmentRewardBinding
import com.example.millionairegameserver.ui.viewmodel.RewardUiState
import com.example.millionairegameserver.ui.viewmodel.RewardViewModel
import kotlinx.coroutines.launch

@UnstableApi class RewardFragment : Fragment() {

    companion object {
        fun newInstance() = RewardFragment()
        private const val TAG = "RewardFragment"
    }

    private lateinit var viewModel: RewardViewModel
    private lateinit var binding: FragmentRewardBinding
    private lateinit var playerview: PlayerView
    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this).get(RewardViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRewardBinding.inflate(inflater, container, false)

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
            viewModel.getCurrentReward()
            viewModel.uiState.collect { currentReward ->
                Log.d(TAG, "onCreateView: ")
                when (currentReward) {
                    is RewardUiState.Error -> showError(currentReward.e)
                    is RewardUiState.Success -> showReward(currentReward.reward)
                }

            }
        }

        return binding.root
    }

    private fun showReward(reward: String) {
        binding.rewardTitle.text = reward
    }

    private fun showError(e: Throwable) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }

}