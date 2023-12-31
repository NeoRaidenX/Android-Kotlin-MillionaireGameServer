package com.example.millionairegameserver.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.navigation.fragment.findNavController
import com.example.millionairegameserver.R
import com.example.millionairegameserver.databinding.FragmentOpeningBinding

@UnstableApi
/**
 * A simple [Fragment] subclass.
 * Use the [OpeningFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OpeningFragment : Fragment() {

    private lateinit var binding: FragmentOpeningBinding
    private lateinit var playerview: PlayerView
    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOpeningBinding.inflate(inflater, container, false)
        playerview = binding.playerview

        exoPlayer = ExoPlayer.Builder(requireContext()).build()
        playerview.player = exoPlayer

        val uri = RawResourceDataSource.buildRawResourceUri(R.raw.opening)
        val mediaItem = MediaItem.fromUri(uri)
        val mediaSource = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(requireContext()))
            .createMediaSource(mediaItem)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()

        exoPlayer.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                Log.d(TAG, "onPlaybackStateChanged: $playbackState")
                if (playbackState == Player.STATE_ENDED) {
                    navigateToIntro()
                }
            }
        })

        return binding.root
    }

    private fun navigateToIntro() {
        val action = OpeningFragmentDirections.actionOpeningFragmentToIntroFragment()
        findNavController().navigate(action)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.stop()
        exoPlayer.release()
    }

    companion object {

        private const val TAG = "OpeningFragment"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OpeningFragment.
         */
        // TODO: Rename and change types and number of parameters
        /*@JvmStatic
        fun newInstance(param1: String, param2: String) =
            OpeningFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }*/
    }
}