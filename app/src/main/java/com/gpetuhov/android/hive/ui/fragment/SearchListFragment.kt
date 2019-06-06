package com.gpetuhov.android.hive.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.arellomobile.mvp.presenter.InjectPresenter
import com.gpetuhov.android.hive.R
import com.gpetuhov.android.hive.databinding.FragmentSearchListBinding
import com.gpetuhov.android.hive.domain.model.User
import com.gpetuhov.android.hive.presentation.presenter.SearchListFragmentPresenter
import com.gpetuhov.android.hive.presentation.view.SearchListFragmentView
import com.gpetuhov.android.hive.ui.fragment.base.BaseFragment
import com.gpetuhov.android.hive.ui.viewmodel.SearchResultViewModel
import com.gpetuhov.android.hive.util.hideMainHeader
import com.gpetuhov.android.hive.util.setActivitySoftInputPan
import com.gpetuhov.android.hive.util.showBottomNavigationView
import com.pawegio.kandroid.toast

class SearchListFragment : BaseFragment(), SearchListFragmentView {

    @InjectPresenter lateinit var presenter: SearchListFragmentPresenter

    private var binding: FragmentSearchListBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Adjust_pan is needed to prevent activity from being pushed up by the keyboard
        setActivitySoftInputPan()

        hideMainHeader()
        showBottomNavigationView()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_list, container, false)
        binding?.presenter = presenter

        val args = SearchListFragmentArgs.fromBundle(arguments!!)
        presenter.queryLatitude = args.queryLatitude.toDouble()
        presenter.queryLongitude = args.queryLongitude.toDouble()
        presenter.queryRadius = args.queryRadius.toDouble()
        presenter.queryText = args.queryText

        val viewModel = ViewModelProviders.of(this).get(SearchResultViewModel::class.java)
        viewModel.searchResult.observe(this, Observer<MutableMap<String, User>> { searchResult ->
            // TODO: update UI
            toast("${searchResult.size}")
        })

        // TODO: Start search in onResume

        return binding?.root
    }

    // === SearchListFragmentView ===

    override fun navigateUp() {
        findNavController().navigateUp()
    }
}