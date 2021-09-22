package com.example.musicloud.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.musicloud.databinding.QuestionItemViewBinding
import com.example.musicloud.support.Question

class HelpAdapter: RecyclerView.Adapter<HelpAdapter.ViewHolder>() {

    class ViewHolder private constructor (val binding: QuestionItemViewBinding):
        RecyclerView.ViewHolder (binding.root) {
        companion object {
            fun from (parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from (parent.context)
                val binding = QuestionItemViewBinding.inflate (inflater, parent, false)
                return ViewHolder (binding)
            }
        }
    }

    private val diffCallBack = object : DiffUtil.ItemCallback<Question> () {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer (this, diffCallBack)
    var questions: List<Question>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from (parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = questions[position]
        holder.binding.questionTextView.text = question.question
        holder.binding.answerTextView.text = question.answer

        holder.binding.revealAnswerButton.setOnClickListener {
            if (holder.binding.answerTextView.visibility == View.VISIBLE) {
                holder.binding.answerTextView.visibility = View.GONE
            }
            else {
                holder.binding.answerTextView.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return questions.size
    }



}