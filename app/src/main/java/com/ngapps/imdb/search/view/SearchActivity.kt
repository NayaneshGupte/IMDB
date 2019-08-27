package com.ngapps.imdb.search.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.ngapps.imdb.R
import com.ngapps.imdb.base.BaseActivity
import com.ngapps.imdb.search.model.Loading
import com.ngapps.imdb.search.model.NetworkError
import com.ngapps.imdb.search.model.NetworkState
import com.ngapps.imdb.search.model.Success
import com.ngapps.imdb.search.view.adapter.RetryListener
import com.ngapps.imdb.search.view.adapter.SearchPagingAdapter
import com.ngapps.imdb.search.viewmodel.SearchViewModel
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.content_search.*
import javax.inject.Inject

class SearchActivity : BaseActivity(), SearchWidget.OnSearchListener, RetryListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: SearchViewModel
    lateinit var adapter: SearchPagingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(toolbar)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)

        //configure search widget
        searchWidget.with(this).removeMinToSearch().build()
        initAdapter()
    }

    private fun initAdapter() {
        val layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        adapter = SearchPagingAdapter(SearchDiffUtilItemCallback(), this)
        recyclerSearch.layoutManager = layoutManager
        recyclerSearch.adapter = adapter
        viewModel.searchResults.observe(this, Observer { adapter.submitList(it) })
        viewModel.networkState.observe(this, Observer {
            setProgress(it)
            adapter.setNetworkState(it)
        })
    }


    private fun setProgress(loadState: NetworkState) {
        when (loadState) {
            is Success -> progress.visibility = View.GONE
            is NetworkError -> {
                progress.visibility = View.GONE
                Toast.makeText(this, loadState.message, Toast.LENGTH_SHORT).show()
            }
            is Loading -> progress.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_search) {
            searchWidget.show()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun changedSearch(text: CharSequence) {
        text.toString().trim().let {
            if (it.isNotEmpty()) {
                setProgress(Loading)
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                if (viewModel.loadData(text.toString())) {
                    recyclerSearch.scrollToPosition(0)
                    adapter.submitList(null)
                    hideKeyboard()
                }
            }
        }
    }

    private fun hideKeyboard() {
        if (searchWidget.hasFocus()) searchWidget.clearFocus()
    }

    override fun retry() {
        viewModel.retry()
    }
}
