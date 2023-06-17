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
import com.example.millionairegameserver.Actions
import com.example.millionairegameserver.App
import com.example.millionairegameserver.ui.viewmodel.ChartViewModel
import com.example.millionairegameserver.R
import com.example.millionairegameserver.databinding.FragmentChartBinding
import com.example.millionairegameserver.datamodel.ChartModel
import com.example.millionairegameserver.ui.viewmodel.ChartUiState
import com.example.millionairegameserver.ui.viewmodel.RewardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@UnstableApi class ChartFragment : Fragment() {

    companion object {
        fun newInstance() = ChartFragment()
        private const val TAG = "ChartFragment"
    }

    private lateinit var viewModel: ChartViewModel
    private lateinit var binding: FragmentChartBinding
    private lateinit var playerview: PlayerView
    private lateinit var exoPlayer: ExoPlayer

    private val actionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                Actions.NAVIGATE_UP -> {
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this).get(ChartViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChartBinding.inflate(inflater, container, false)

        initPlayer()
        initView()

        return binding.root
    }

    private fun initView() {
        lifecycleScope.launch {
            viewModel.uiState.collect { chartResult ->
                Log.d(TAG, "chartResult: ")
                when(chartResult) {
                    is ChartUiState.Error -> showError(chartResult.e)
                    is ChartUiState.Success -> updateViews(chartResult.chartModel)
                }
            }
        }
        lifecycleScope.launch {
            delay(2000)
            viewModel.loadChartResult()
            delay(2000)
            viewModel.showChartResult()
        }
    }

    private fun updateViews(chartModel: ChartModel) {
        binding.progressA.progress = chartModel.ansAPercentage
        binding.chartAPercentage.text = chartModel.ansAPercentage.toString() + "%"
        binding.progressB.progress = chartModel.ansBPercentage
        binding.chartBPercentage.text = chartModel.ansBPercentage.toString() + "%"
        binding.progressC.progress = chartModel.ansCPercentage
        binding.chartCPercentage.text = chartModel.ansCPercentage.toString() + "%"
        binding.progressD.progress = chartModel.ansDPercentage
        binding.chartDPercentage.text = chartModel.ansDPercentage.toString() + "%"
    }

    private fun initPlayer() {
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
        intentFilter.addAction(Actions.NAVIGATE_UP)
        return intentFilter
    }

    private fun showError(e: Throwable) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }

}