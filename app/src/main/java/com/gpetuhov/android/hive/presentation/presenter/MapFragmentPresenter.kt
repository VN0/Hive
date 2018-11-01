package com.gpetuhov.android.hive.presentation.presenter

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.gpetuhov.android.hive.application.HiveApp
import com.gpetuhov.android.hive.domain.repository.Repo
import com.gpetuhov.android.hive.managers.MapManager
import com.gpetuhov.android.hive.presentation.view.MapFragmentView
import javax.inject.Inject

@InjectViewState
class MapFragmentPresenter : MvpPresenter<MapFragmentView>() {

    @Inject lateinit var mapManager: MapManager
    @Inject lateinit var repo: Repo

    // Current query text from search EditText
    // (binded to view with two-way data binding).
    var queryText = ""

    init {
        HiveApp.appComponent.inject(this)
    }

    fun search() = repo.search(queryText)

    fun cancelSearch() {
        viewState.clearSearch()
        search()
    }

    fun moveToCurrentLocation() = mapManager.moveToCurrentLocation()

    fun zoomIn() = mapManager.zoomIn()

    fun zoomOut() = mapManager.zoomOut()
}