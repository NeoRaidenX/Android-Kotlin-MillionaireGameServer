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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.millionairegameserver.Actions
import com.example.millionairegameserver.App
import com.example.millionairegameserver.bluetooth.BluetoothUiState
import com.example.millionairegameserver.bluetooth.BluetoothViewModel
import com.example.millionairegameserver.ui.viewmodel.MainViewModel
import com.example.millionairegameserver.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    companion object {
        fun newInstance() = MainFragment()
        private const val TAG = "MainFragment"
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var btViewModel: BluetoothViewModel

    private val actionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive: ${intent.action}")
            when(intent.action) {
                Actions.NAVIGATE_OPEN -> {
                    navigateToOpening()
                }
                Actions.NAVIGATE_QUEST -> {
                    navigateToQuestion()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        btViewModel = ViewModelProvider(this).get(BluetoothViewModel::class.java)
        btViewModel.startListening()
        viewModel.loadQuestionsToDatabase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMainBinding.inflate(inflater, container, false)
        lifecycleScope.launch {
            btViewModel.uiState.collect { btState ->
                when (btState) {
                    is BluetoothUiState.Connecting -> {
                        Log.d(TAG, "Connecting: ")
                    }
                    is BluetoothUiState.Error -> {
                        Log.d(TAG, "Error: ")
                        binding.btStatus.text = "Error"
                        binding.progressbar.visibility = View.GONE
                    }
                    is BluetoothUiState.Success -> {
                        Log.d(TAG, "Success: ")
                        binding.btStatus.text = "Connected :)"
                        binding.progressbar.visibility = View.GONE
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    private fun navigateToOpening() {
        val action = MainFragmentDirections.actionMainFragmentToOpeningFragment2()
        findNavController().navigate(action)
    }

    private fun navigateToQuestion() {
        val action = MainFragmentDirections.actionMainFragmentToQuestionFragment()
        findNavController().navigate(action)
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
        intentFilter.addAction(Actions.NAVIGATE_OPEN)
        intentFilter.addAction(Actions.NAVIGATE_QUEST)
        return intentFilter
    }

}