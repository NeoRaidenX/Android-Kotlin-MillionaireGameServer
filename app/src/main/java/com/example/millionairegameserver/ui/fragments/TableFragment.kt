package com.example.millionairegameserver.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.example.millionairegameserver.R
import com.example.millionairegameserver.databinding.FragmentQuestionBinding
import com.example.millionairegameserver.databinding.FragmentTableBinding
import com.example.millionairegameserver.ui.viewmodel.QuestionViewModel
import com.example.millionairegameserver.ui.viewmodel.TableViewModel

@UnstableApi class TableFragment : Fragment() {

    companion object {
        fun newInstance() = TableFragment()
        private const val TAG = "TableFragment"
    }

    private lateinit var viewModel: TableViewModel
    private lateinit var binding: FragmentTableBinding

    private lateinit var playerview: PlayerView
    private lateinit var exoPlayer: ExoPlayer

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



        return binding.root
    }

}