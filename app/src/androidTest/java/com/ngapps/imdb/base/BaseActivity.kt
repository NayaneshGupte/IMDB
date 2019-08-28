package com.ngapps.imdb.base

import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.test.espresso.IdlingResource
import com.ngapps.imdb.SimpleIdlingResource


@Suppress("CAST_NEVER_SUCCEEDS")
open class BaseActivity : AppCompatActivity() {

    // The Idling Resource which will be null in production.
    private var mIdlingResource: IdlingResource? = null

    /**
     * Only called from test, creates and returns a new [SimpleIdlingResource].
     */
    @VisibleForTesting
    fun getIdlingResource(): IdlingResource {
        if (mIdlingResource == null) {
            mIdlingResource = SimpleIdlingResource() as IdlingResource
        }
        return mIdlingResource!!
    }
}