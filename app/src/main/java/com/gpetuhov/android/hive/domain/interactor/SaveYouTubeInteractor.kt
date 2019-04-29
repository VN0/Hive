package com.gpetuhov.android.hive.domain.interactor

import com.gpetuhov.android.hive.application.HiveApp
import com.gpetuhov.android.hive.domain.repository.Repo
import com.gpetuhov.android.hive.domain.util.ResultMessages
import javax.inject.Inject

class SaveYouTubeInteractor(private val callback: Callback) : Interactor {

    interface Callback {
        fun onSaveYouTubeError(errorMessage: String)
    }

    @Inject lateinit var repo: Repo
    @Inject lateinit var resultMessages: ResultMessages

    private var newYouTube = ""

    init {
        HiveApp.appComponent.inject(this)
    }

    // Do not call this directly!
    override fun execute() {
        repo.saveUserYouTube(newYouTube) { callback.onSaveYouTubeError(resultMessages.getSaveYouTubeErrorMessage()) }
    }

    // Call this method to save new YouTube
    fun saveYouTube(newYouTube: String) {
        this.newYouTube = newYouTube
        execute()
    }
}