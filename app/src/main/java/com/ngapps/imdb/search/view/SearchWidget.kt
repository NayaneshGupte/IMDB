package com.ngapps.imdb.search.view

import android.animation.Animator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnKeyListener
import android.view.ViewAnimationUtils
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import com.ngapps.imdb.R
import java.util.*

class SearchWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var mTimer: Timer? = null
    private var mContext: Activity? = null

    private var mEdtSearch: EditText? = null
    private var mImgArrow: ImageView? = null
    private var mImgVoice: ImageView? = null
    private var mImgClose: ImageView? = null

    var isVoice = true
        private set
    //endregion

    //region Methods custom
    /**
     * If SearchView is active(show), this method returns the value true
     */
    var isActive = false
        private set
    private var hideKeyboard = false
    private var imeActionSearch = false

    private var colorIcon = -1
        set(colorIcon) {
            field = colorIcon
            this.colorIcon()
        }
    private var mMinToSearch = 4
    private var mSearchDelay = 800

    private var mColorIconArrow = R.color.search_icon
    private var mColorIconVoice = R.color.search_icon
    private var mColorIconClose = R.color.search_icon

    private var mColorPrimaryDark: Int = 0
    private var mStatusBarHideColor = -1
    private var mStatusBarShowColor = -1

    private var mViewSearch: RelativeLayout? = null

    private var mSearchListener: OnSearchListener? = null
    private var mHideSearchListener: OnHideSearchListener? = null

    private var colorIconArrow: Int
        get() = ContextCompat.getColor(mContext!!, mColorIconArrow)
        set(color) {
            this.mColorIconArrow = color
            this.colorIconArrow()
        }

    private var colorIconVoice: Int
        get() = ContextCompat.getColor(mContext!!, mColorIconVoice)
        set(color) {
            this.mColorIconVoice = color
            this.colorIconVoice()
        }

    private var colorIconClose: Int
        get() = ContextCompat.getColor(mContext!!, mColorIconClose)
        set(color) {
            this.mColorIconClose = color
            this.colorIconClose()
        }
    //endregion

    //region Methods Listener
    private val onKeyListener = OnKeyListener { _, keyCode, event ->
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                hide()
                return@OnKeyListener true
            }
        }
        false
    }

    private val onEditorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH && imeActionSearch) {
            if (mSearchListener != null) {
                mSearchListener!!.changedSearch(queryText())
            }

            hide()
            return@OnEditorActionListener true
        }

        hide()
        false
    }

    private val onClickSearch = OnClickListener { hide() }

    private val onClickVoice = OnClickListener { startVoice() }

    private val onClickClose = OnClickListener {
        if (!queryText().isEmpty()) {
            mEdtSearch!!.setText("")

            showKeyboardRunOnUiThread()
        }
    }

    interface OnSearchListener {
        fun changedSearch(text: CharSequence)
    }

    interface OnHideSearchListener {
        fun hideSearch()
    }

    init {
        if (!isInEditMode) {
            initView(context)
            initViewAttribute(context, attrs, defStyleAttr)
        }
    }

    //region Methods init
    @SuppressLint("CutPasteId")
    private fun initView(context: Context) {
        val view = LayoutInflater.from(context).inflate(
            R.layout.search, this, true
        )

        mImgArrow = view.findViewById(R.id.img_arrow)
        mImgVoice = view.findViewById(R.id.img_voice)
        mImgClose = view.findViewById(R.id.img_close)

        mEdtSearch = view.findViewById(R.id.edt_search)
        mImgClose!!.visibility = if (isVoice) View.GONE else View.VISIBLE

        mViewSearch = view.findViewById(R.id.view_search)
        mViewSearch!!.visibility = View.INVISIBLE

        mEdtSearch!!.setOnKeyListener(onKeyListener)

        mImgArrow!!.setOnClickListener(onClickSearch)
        mImgVoice!!.setOnClickListener(onClickVoice)
        mImgClose!!.setOnClickListener(onClickClose)

        mEdtSearch!!.setOnEditorActionListener(onEditorActionListener)
        mEdtSearch!!.addTextChangedListener(OnTextWatcherEdtSearch())
    }

    private fun initViewAttribute(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int
    ) {
        val attr = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.SearchWidget, defStyleAttr, 0
        )
        if (attr != null) {
            try {

                if (attr.hasValue(R.styleable.SearchWidget_searchWidgetHint)) {
                    hint(attr.getString(R.styleable.SearchWidget_searchWidgetHint))
                }

                if (attr.hasValue(R.styleable.SearchWidget_searchWidgetTextColor)) {
                    mEdtSearch!!.setTextColor(
                        attr.getColor(
                            R.styleable.SearchWidget_searchWidgetTextColor, -1
                        )
                    )
                }

                if (attr.hasValue(R.styleable.SearchWidget_searchWidgetHintColor)) {
                    mEdtSearch!!.setHintTextColor(
                        attr.getColor(
                            R.styleable.SearchWidget_searchWidgetHintColor, -1
                        )
                    )
                }

                if (attr.hasValue(R.styleable.SearchWidget_searchWidgetColorIcon)) {
                    colorIcon = attr.getColor(
                        R.styleable.SearchWidget_searchWidgetColorIcon, -1
                    )
                }

                if (attr.hasValue(R.styleable.SearchWidget_searchWidgetColorArrow)) {
                    colorIconArrow = attr.getColor(
                        R.styleable.SearchWidget_searchWidgetColorArrow, -1
                    )
                }

                if (attr.hasValue(R.styleable.SearchWidget_searchWidgetColorVoice)) {
                    colorIconVoice = attr.getColor(
                        R.styleable.SearchWidget_searchWidgetColorVoice, -1
                    )
                }

                if (attr.hasValue(R.styleable.SearchWidget_searchWidgetColorClose)) {
                    colorIconClose = attr.getColor(
                        R.styleable.SearchWidget_searchWidgetColorClose, -1
                    )
                }

                if (attr.hasValue(R.styleable.SearchWidget_searchWidgetBackground)) {
                    mViewSearch!!.setBackgroundColor(
                        attr.getColor(
                            R.styleable.SearchWidget_searchWidgetBackground, -1
                        )
                    )
                }

                if (attr.hasValue(R.styleable.SearchWidget_searchWidgetStatusBarShowColor)) {
                    setStatusBarShowColor(
                        attr.getColor(
                            R.styleable.SearchWidget_searchWidgetStatusBarShowColor, -1
                        )
                    )
                }

                if (attr.hasValue(R.styleable.SearchWidget_searchWidgetStatusBarHideColor)) {
                    setStatusBarHideColor(
                        attr.getColor(
                            R.styleable.SearchWidget_searchWidgetStatusBarHideColor, -1
                        )
                    )
                }
            } finally {
                attr.recycle()
            }
        }
    }
    //endregion

    //region Methods with - build

    /**
     * Start context and the listener Search Live library.
     * Use this method when you are using an Activity
     *
     * @param context - Context Activity
     */
    fun with(context: Context): SearchWidget {

        if (this.mContext == null) {
            try {
                this.mContext = context as Activity
                this.mSearchListener = context as OnSearchListener
            } catch (ignored: ClassCastException) {

            }

        } else {
            build()
        }

        return this
    }

    /**
     * Start context and the listener Search Live library.
     * Use this method when you are using an Fragment
     *
     * @param getActivity - Context Fragment
     * @param context     - Listener
     */
    fun with(getActivity: Activity, context: OnSearchListener): SearchWidget {

        if (this.mContext == null) {
            try {
                this.mContext = getActivity
                this.mSearchListener = context
            } catch (ignored: ClassCastException) {
            }

        } else {
            build()
        }

        return this
    }

    /**
     * Use when you want to know the exact moment that SearchWidget is hidden
     *
     * @param onHideSearchListener - Listener
     */
    fun hideSearch(onHideSearchListener: OnHideSearchListener): SearchWidget {
        this.mHideSearchListener = onHideSearchListener
        return this
    }

    fun build() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val theme = this.mContext!!.theme
                val typedArray = theme.obtainStyledAttributes(
                    intArrayOf(android.R.attr.colorPrimaryDark)
                )

                this.mColorPrimaryDark = ContextCompat.getColor(
                    mContext!!,
                    typedArray.getResourceId(0, 0)
                )
            }
        } catch (e: Exception) {
            e.stackTrace
        }

    }

    /**
     * Hiding the keyboard as soon as you finish it
     *
     *
     */
    fun hideKeyboardAfterSearch(): SearchWidget {
        this.hideKeyboard = true
        return this
    }

    /**
     * Time in milliseconds of delay. Only after the given time will the search be made.
     *
     * @param delay default 800
     */
    fun searchDelay(delay: Int): SearchWidget {
        this.mSearchDelay = delay
        return this
    }

    /**
     * Remove search delay
     *
     *
     */
    fun removeSearchDelay(): SearchWidget {
        this.mSearchDelay = 0
        return this
    }


    /**
     * Minimum number of characters to start the search
     *
     * @param minToSearch default >= 4
     */
    fun minToSearch(minToSearch: Int): SearchWidget {
        this.mMinToSearch = minToSearch
        return this
    }

    fun removeMinToSearch(): SearchWidget {
        this.mMinToSearch = 0
        return this
    }

    /**
     * Set a new background color. If you do not use this method and standard color is white SearchWidget.
     * In his layout.xml you can use the "app:searchWidgetBackground="@color/..."" attribute
     *
     * @param resId color attribute - colors.xml file
     */
    fun backgroundResource(resId: Int): SearchWidget {
        mViewSearch!!.setBackgroundResource(resId)
        return this
    }

    /**
     * Set a new background color. If you do not use this method and standard color is white SearchWidget.
     * In his layout.xml you can use the "app:searchWidgetBackground="@color/..."" attribute
     *
     * @param color color attribute - colors.xml file
     */
    fun backgroundColor(color: Int): SearchWidget {
        mViewSearch!!.setBackgroundColor(ContextCompat.getColor(mContext!!, color))
        return this
    }

    /**
     * Set a new text color.
     * In his layout.xml you can use the "app:searchWidgetTextColor="@color/..."" attribute
     *
     * @param color color attribute - colors.xml file
     */
    fun textColor(color: Int): SearchWidget {
        mEdtSearch!!.setTextColor(ContextCompat.getColor(mContext!!, color))
        return this
    }

    /**
     * Set a new hint color.
     * In his layout.xml you can use the "app:searchWidgetHintColor="@color/..."" attribute
     *
     * @param color color attribute - colors.xml file
     */
    fun hintColor(color: Int): SearchWidget {
        mEdtSearch!!.setHintTextColor(ContextCompat.getColor(mContext!!, color))
        return this
    }

    /**
     * Set a new text.
     *
     * @param text "valeu"
     */
    fun text(text: String): SearchWidget {
        mEdtSearch!!.setText(text)
        return this
    }

    /**
     * Set a new text.
     *
     * @param text string attribute - string.xml file
     */
    fun text(text: Int): SearchWidget {
        mEdtSearch!!.setText(mContext!!.getString(text))
        return this
    }

    /**
     * Set a new hint.
     * In his layout.xml you can use the "app:searchWidgetHint="value"" attribute
     *
     * @param text "valeu"
     */
    fun hint(text: String?): SearchWidget {
        mEdtSearch!!.hint = text
        return this
    }

    /**
     * Set a new hint.
     * In his layout.xml you can use the "app:searchWidgetHint="@string/..."" attribute
     *
     * @param text string attribute - string.xml file
     */
    fun hint(text: Int): SearchWidget {
        mEdtSearch!!.hint = mContext!!.getString(text)
        return this
    }

    /**
     * Set a new color for all icons (arrow, voice and close).
     * In his layout.xml you can use the "app:searchWidgetColorIcon="@color/..."" attribute
     *
     * @param color color attribute - colors.xml file
     */
    fun colorIcon(color: Int): SearchWidget {
        this.colorIcon = ContextCompat.getColor(mContext!!, color)
        return this
    }

    /**
     * Set a new color for back arrow
     * In his layout.xml you can use the "app:searchWidgetColorArrow="@color/..."" attribute
     *
     * @param color color attribute - colors.xml file
     */
    fun colorIconArrow(color: Int): SearchWidget {
        this.colorIconArrow = ContextCompat.getColor(mContext!!, color)
        return this
    }

    /**
     * Set a new color for voice
     * In his layout.xml you can use the "app:searchWidgetColorVoice="@color/..."" attribute
     *
     * @param color color attribute - colors.xml file
     */
    fun colorIconVoice(color: Int): SearchWidget {
        this.colorIconVoice = ContextCompat.getColor(mContext!!, color)
        return this
    }

    /**
     * Returns the value typed
     */
    fun queryText(): String {
        return mEdtSearch!!.text.toString().trim { it <= ' ' }
    }

    /**
     * Set a new color for close
     * In his layout.xml you can use the "app:searchWidgetColorClose="@color/..."" attribute
     *
     * @param color color attribute - colors.xml file
     */
    fun colorIconClose(color: Int): SearchWidget {
        this.colorIconClose = ContextCompat.getColor(mContext!!, color)
        return this
    }

    /**
     * Set a new color for statusBar when the SearchWidget is closed
     * In his layout.xml you can use the "app:searchWidgetStatusBarHideColor="@color/..."" attribute
     *
     * @param color color attribute - colors.xml file
     */
    fun statusBarHideColor(color: Int): SearchWidget {
        setStatusBarHideColor(ContextCompat.getColor(mContext!!, color))
        return this
    }

    /**
     * Set a new color for statusBar when the SearchWidget for visible
     * In his layout.xml you can use the "app:searchWidgetStatusBarShowColor="@color/..."" attribute
     *
     * @param color color attribute - colors.xml file
     */
    fun statusBarShowColor(color: Int): SearchWidget {
        setStatusBarShowColor(ContextCompat.getColor(mContext!!, color))
        return this
    }

    /**
     * By enabling imeActionSearch you are only activating the search when you click the imeActionSearch on the keyboard.
     * This disables the search as you type.
     *
     *
     */
    fun imeActionSearch(): SearchWidget {
        this.imeActionSearch = true
        return this
    }

    /**
     * Hide voice icon
     */
    fun hideVoice(): SearchWidget {
        isVoice = false
        mImgVoice!!.visibility = View.GONE
        return this
    }

    /**
     * Show voice icon
     */
    fun showVoice(): SearchWidget {
        isVoice = true
        mImgVoice!!.visibility = View.VISIBLE
        return this
    }

    private fun colorIcon() {
        if (colorIcon != -1 && colorIconArrow == -1) {
            mImgArrow!!.setColorFilter(this.colorIcon)
        }

        if (colorIcon != -1 && colorIconVoice == -1) {
            mImgVoice!!.setColorFilter(this.colorIcon)
        }

        if (colorIcon != -1 && colorIconClose == -1) {
            mImgClose!!.setColorFilter(this.colorIcon)
        }
    }

    private fun colorIconArrow() {
        if (colorIconArrow != -1) {
            mImgArrow!!.setColorFilter(this.colorIconArrow)
        }
    }

    private fun colorIconVoice() {
        if (this.colorIconVoice != -1) {
            mImgVoice!!.setColorFilter(this.colorIconVoice)
        } else {
            mImgVoice!!.clearColorFilter()
        }
    }

    private fun colorIconClose() {
        if (this.colorIconClose != -1) {
            mImgClose!!.setColorFilter(this.colorIconClose)
        } else {
            mImgClose!!.clearColorFilter()
        }
    }

    private fun setStatusBarHideColor(statusBarHideColor: Int) {
        this.mStatusBarHideColor = statusBarHideColor
    }

    private fun setStatusBarShowColor(statusBarShowColor: Int) {
        this.mStatusBarShowColor = statusBarShowColor
    }

    private inner class OnTextWatcherEdtSearch : TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            try {

                if (mTimer != null) {
                    mTimer!!.cancel()
                }

                if (queryText().isEmpty()) {
                    mImgClose!!.visibility = if (isVoice) View.GONE else View.VISIBLE
                    mImgVoice!!.visibility = if (isVoice) View.VISIBLE else View.GONE
                    mImgVoice!!.setImageResource(R.drawable.ic_voice)
                    colorIconVoice()
                } else {
                    mImgVoice!!.visibility = View.GONE
                    mImgClose!!.visibility = View.VISIBLE
                    mImgClose!!.setImageResource(R.drawable.ic_close)
                    colorIconClose()
                }

                colorIcon()
                colorIconArrow()
            } catch (e: Exception) {
                e.stackTrace
            }

        }

        override fun afterTextChanged(s: Editable) {
            if (imeActionSearch) {
                return
            }

            if (s.length >= mMinToSearch) {

                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        // TODO: do what you need here (refresh list)
                        if (mTimer != null) {
                            mTimer!!.cancel()
                            mContext!!.runOnUiThread {
                                if (mSearchListener != null) {
                                    mSearchListener!!.changedSearch(queryText())
                                }

                                if (hideKeyboard) {
                                    hideKeyboard()
                                }
                            }
                        }
                    }

                }, mSearchDelay.toLong())
            }
        }
    }
    //endregion

    //region Methods animation
    /**
     * Hide SearchWidget
     */
    fun hide() {
        try {
            hideAnimation()
            isActive = false
        } catch (e: Exception) {
            e.stackTrace
        }

    }

    /**
     * Show SearchWidget
     */
    fun show() {
        isActive = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            try {
                showAnimation()
            } catch (ignored: ClassCastException) {
            }

        } else {

            val fadeIn = AnimationUtils.loadAnimation(
                mContext!!.applicationContext, android.R.anim.fade_in
            )

            mViewSearch!!.isEnabled = true
            mViewSearch!!.visibility = View.VISIBLE
            mViewSearch!!.animation = fadeIn

            showKeyboardRunOnUiThread()
        }

        mEdtSearch!!.requestFocus()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showAnimation() {
        try {

            val DEFAULT_STATUS_BAR_SHOW_COLOR = -1

            mContext?.window?.statusBarColor =
                if (mStatusBarShowColor != DEFAULT_STATUS_BAR_SHOW_COLOR)
                    mStatusBarShowColor
                else
                    ContextCompat.getColor(
                        mContext!!, R.color.search_primary_dark
                    )

            val animator = ViewAnimationUtils.createCircularReveal(
                mViewSearch,
                mViewSearch!!.width - dpToPixel(24f, this.mContext!!).toInt(),
                dpToPixel(23f, this.mContext!!).toInt(), 0f,
                Math.hypot(
                    mViewSearch!!.width.toDouble(),
                    mViewSearch!!.height.toDouble()
                ).toFloat()
            )
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    showKeyboardRunOnUiThread()
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })

            animator.duration = 300
            animator.start()
        } catch (e: Exception) {
            e.stackTrace
            mContext?.runOnUiThread { showKeyboardRunOnUiThread() }
        }

        mViewSearch?.visibility = View.VISIBLE
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun hideAnimation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            val DEFAULT_STATUS_BAR_HIDE_COLOR = -1

            mContext!!.window.statusBarColor =
                if (mStatusBarHideColor != DEFAULT_STATUS_BAR_HIDE_COLOR)
                    mStatusBarHideColor
                else
                    mColorPrimaryDark

            val animatorHide = ViewAnimationUtils.createCircularReveal(
                mViewSearch,
                mViewSearch!!.width - dpToPixel(24f, mContext!!).toInt(),
                dpToPixel(23f, mContext!!).toInt(),
                Math.hypot(
                    mViewSearch!!.width.toDouble(),
                    mViewSearch!!.height.toDouble()
                ).toFloat(), 0f
            )
            animatorHide.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    mContext!!.runOnUiThread { hideKeyboard() }
                }

                override fun onAnimationEnd(animation: Animator) {
                    mViewSearch!!.visibility = View.GONE

                    if (mHideSearchListener != null) {
                        mHideSearchListener!!.hideSearch()
                    }
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            animatorHide.duration = 200
            animatorHide.start()

        } else {

            mContext!!.runOnUiThread { hideKeyboard() }

            val mFadeOut = AnimationUtils.loadAnimation(
                mContext!!.applicationContext, android.R.anim.fade_out
            )

            mViewSearch!!.animation = mFadeOut
            mViewSearch!!.visibility = View.INVISIBLE

            if (mHideSearchListener != null) {
                mHideSearchListener!!.hideSearch()
            }
        }

        mEdtSearch!!.setText("")
        mViewSearch!!.isEnabled = false
    }

    private fun dpToPixel(dp: Float, context: Context): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi / 160f)
    }
    //endregion

    //region Methods InstanceState
    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(SEARCH_LIVEO_INSTANCE_STATE, super.onSaveInstanceState())
        bundle.putBoolean(SEARCH_LIVEO_STATE_TO_SAVE, this.isActive)

        if (!queryText().isEmpty()) {
            bundle.putString(SEARCH_LIVEO_SEARCH_TEXT, queryText())
        }

        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        var localState = state

        if (localState is Bundle) {
            val bundle = localState as Bundle?
            this.isActive = bundle!!.getBoolean(SEARCH_LIVEO_STATE_TO_SAVE)

            val text = bundle.getString(SEARCH_LIVEO_SEARCH_TEXT, "")
            if (text.trim { it <= ' ' } != "") {
                mEdtSearch!!.setText(text)
            }

            if (this.isActive) {
                show()
            }

            localState = bundle.getParcelable(SEARCH_LIVEO_INSTANCE_STATE)
        }

        super.onRestoreInstanceState(localState)
    }
    //endregion

    //region Methods keyboard
    private fun showKeyboardRunOnUiThread() {
        if (mContext != null && !mContext!!.isFinishing) {
            mContext!!.runOnUiThread {
                val inputMethodManager =
                    mContext!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                inputMethodManager.toggleSoftInput(
                    InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }
        }
    }

    private fun hideKeyboard() {
        if (mContext != null && !mContext!!.isFinishing) {
            val inputMethodManager =
                mContext!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            if (mContext != null) {
                inputMethodManager.hideSoftInputFromWindow(
                    mViewSearch!!.windowToken, 0
                )
            }
        }
    }
    //endregion

    //region Methods voice
    private fun startVoice() {
        hideKeyboard()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            mContext!!.getString(R.string.search_view_voice)
        )

        try {
            mContext!!.startActivityForResult(
                intent,
                REQUEST_CODE_SPEECH_INPUT
            )
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(
                mContext!!.applicationContext,
                R.string.not_supported,
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    fun resultVoice(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == Activity.RESULT_OK && null != data) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)

                if (mSearchListener != null) {
                    mEdtSearch!!.setText(result[0])
                    mSearchListener!!.changedSearch(result[0])
                }
            }
        }
    }

    companion object {

        var REQUEST_CODE_SPEECH_INPUT = 7777

        private val SEARCH_LIVEO_SEARCH_TEXT = "searchText"
        private val SEARCH_LIVEO_STATE_TO_SAVE = "stateToSave"
        private val SEARCH_LIVEO_INSTANCE_STATE = "instanceState"
    }
    //endregion
}