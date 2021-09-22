package com.example.musicloud.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicloud.R
import com.example.musicloud.support.Question

class HelpViewModel (
    application: Application
): AndroidViewModel (application) {

    private val _questions = MutableLiveData <List<Question>> ()
    val questions: LiveData<List<Question>> get() = _questions

    init {
        getQuestionsAndAnswers()
    }

    private fun getQuestionsAndAnswers () {
        val ques = getApplication<Application>().resources.getStringArray (R.array.questions)
        val ans = getApplication<Application>().resources.getStringArray (R.array.answers)
        val questionList = mutableListOf<Question>()
        var idx = 0
        while (idx < ques.size) {
            questionList.add (Question (ques[idx], ans[idx]))
            idx++
        }
        _questions.postValue (questionList)
    }
}