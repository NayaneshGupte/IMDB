package com.ngapps.imdb.search.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.ngapps.imdb.R
import com.ngapps.imdb.databinding.ItemLoadingListBinding
import com.ngapps.imdb.databinding.ItemNetworkFailureBinding
import com.ngapps.imdb.databinding.ItemSearchResultBinding
import com.ngapps.imdb.search.model.*


class SearchPagingAdapter constructor(
    diffUtilItemCallback: DiffUtil.ItemCallback<SearchViewData>,
    private val retryListener: RetryListener
) : PagedListAdapter<SearchViewData, RecyclerView.ViewHolder>(diffUtilItemCallback) {

    private lateinit var context: Context
    private var currentNetworkState: NetworkState = Loading

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        this.context = parent.context
        val layoutInflater = LayoutInflater.from(parent.context)

        when (viewType) {
            R.layout.item_loading_list -> {
                val loadingListItemBinding =
                    ItemLoadingListBinding.inflate(layoutInflater, parent, false)
                return LoadingViewHolder(loadingListItemBinding)
            }
            R.layout.item_network_failure -> {
                val networkFailureListItemBinding =
                    ItemNetworkFailureBinding.inflate(layoutInflater, parent, false)
                networkFailureListItemBinding.retry.setOnClickListener { retryListener.retry() }
                return NetworkErrorViewHolder(networkFailureListItemBinding)
            }
            else -> {
                val searchResultItemBinding =
                    ItemSearchResultBinding.inflate(layoutInflater, parent, false)
                return SearchViewHolder(searchResultItemBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (getItemViewType(position)) {
            R.layout.item_search_result -> {
                val searchViewData = getItem(position) as SearchViewData
                val searchViewHolder = holder as SearchViewHolder
                configureImage(searchViewHolder, searchViewData)
            }
            R.layout.item_loading_list -> {
                val loadingViewHolder = holder as LoadingViewHolder
                loadingViewHolder.binding.spinner.visibility = View.VISIBLE
            }
            R.layout.item_network_failure -> {

                val networkErrorViewHolder = holder as NetworkErrorViewHolder
                val networkError = currentNetworkState as NetworkError
                networkErrorViewHolder.binding.networkPbm.text = networkError.message
            }
        }
    }

    private fun configureImage(holder: SearchViewHolder, searchViewData: SearchViewData) {
        Glide.with(context)
            .load(searchViewData.poster)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.binding.showImage)
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            if (currentNetworkState === Loading) {
                R.layout.item_loading_list
            } else {
                R.layout.item_network_failure
            }
        } else {
            R.layout.item_search_result
        }
    }

    private fun hasExtraRow(): Boolean {
        return currentNetworkState != Success
    }


    fun setNetworkState(newNetworkState: NetworkState) {
        val previousState = currentNetworkState
        val previousExtraRow = hasExtraRow()
        currentNetworkState = newNetworkState
        val newExtraRow = hasExtraRow()
        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(itemCount)
            } else {
                notifyItemInserted(itemCount)
            }
        } else if (newExtraRow && previousState !== newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }
}

class SearchViewHolder(val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root)

class LoadingViewHolder(val binding: ItemLoadingListBinding) : RecyclerView.ViewHolder(binding.root)

class NetworkErrorViewHolder(val binding: ItemNetworkFailureBinding) :
    RecyclerView.ViewHolder(binding.root)

interface RetryListener {
    fun retry()
}