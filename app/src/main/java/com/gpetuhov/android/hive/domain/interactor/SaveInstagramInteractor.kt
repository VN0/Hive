package com.gpetuhov.android.hive.domain.interactor

import com.gpetuhov.android.hive.application.HiveApp
import com.gpetuhov.android.hive.domain.interactor.base.Interactor
import com.gpetuhov.android.hive.domain.repository.Repo
import com.gpetuhov.android.hive.domain.util.ResultMessages
import javax.inject.Inject

class SaveInstagramInteractor(private val callback: Callback) : Interactor {

    interface Callback {
        fun onSaveInstagramError(errorMessage: String)
    }

    @Inject lateinit var repo: Repo
    @Inject lateinit var resultMessages: ResultMessages

    private var newInstagram = ""

    init {
        HiveApp.appComponent.inject(this)
    }

    // Do not call this directly!
    override fun execute() {
        repo.saveUserInstagram(newInstagram) { callback.onSaveInstagramError(resultMessages.getSaveInstagramErrorMessage()) }
    }

    // Call this method to save new Instagram
    fun saveInstagram(newInstagram: String) {
        this.newInstagram = newInstagram
        execute()
    }
}