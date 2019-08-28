package com.ngapps.imdb.search

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ngapps.imdb.search.view.SearchActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchWidgetTest {

    private var simpleIdlingResource: IdlingResource? = null

    @Before
    fun registerIdlingResource() {

        val scenario = ActivityScenario.launch(SearchActivity::class.java)
        scenario.onActivity {
            simpleIdlingResource = it.getIdlingResource()

            IdlingRegistry.getInstance().unregister(simpleIdlingResource)
        }
    }


    @Test
    fun type_search_query() {

      
    }

    fun unregisterIdlingResource() {
        simpleIdlingResource?.let {
            IdlingRegistry.getInstance().unregister(simpleIdlingResource)
        }
    }

    companion object {

        const val STRING_TO_BE_TYPED = "Avengers"
    }
}