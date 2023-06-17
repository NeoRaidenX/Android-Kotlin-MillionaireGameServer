package com.example.millionairegameserver.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.example.millionairegameserver.R
import com.example.millionairegameserver.RewardTableEnum
import com.example.millionairegameserver.databinding.RowRewardBinding

class RewardTableAdapter(val context: Context) : Adapter<RewardTableAdapter.ViewHolder>() {

    private var currentRewardPosition = 0

    inner class ViewHolder(val binding: RowRewardBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(reward: RewardTableEnum) = binding.apply {
            binding.rowRewardNumber.text = (reward.ordinal + 1).toString()
            binding.rowRewardTitle.text = reward.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return RewardTableEnum.values().size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(RewardTableEnum.values()[position])
        if (position == currentRewardPosition) {
            holder.binding.cardview.setCardBackgroundColor(context.resources.getColor(R.color.reward_bg))
        }
    }

    fun updateCurrentPosition(position: Int) {
        currentRewardPosition = position
        notifyDataSetChanged()
    }
}