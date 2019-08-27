package com.ngapps.imdb.search.view

import androidx.recyclerview.widget.DiffUtil
import com.ngapps.imdb.search.model.SearchViewData


class SearchDiffUtilItemCallback : DiffUtil.ItemCallback<SearchViewData>() {
    override fun areItemsTheSame(oldItem: SearchViewData, newItem: SearchViewData): Boolean {
        return oldItem.imdbId == newItem.imdbId
    }

    override fun areContentsTheSame(oldItem: SearchViewData, newItem: SearchViewData): Boolean {
        return oldItem.title == newItem.title
    }
}