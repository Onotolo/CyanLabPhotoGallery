package comonotolo.httpsgithub.cyanlabphotogallery.activities

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewPropertyAnimator
import comonotolo.httpsgithub.cyanlabphotogallery.R
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.FavoriteFragment
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.GalleryFragment
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.RecentFragment
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.TopFragment
import kotlinx.android.synthetic.main.activity_main.*


/**
 *  Application's main activity, which contains {@link android.support.v4.view.ViewPager}
 *  with three different fragments {@link import android.support.v4.app.Fragment}
 */
class MainActivity : AppCompatActivity() {

    companion object {

        const val INTENT_EXTRA_POSITION = "Image position"
        const val INTENT_EXTRA_IMAGES_NAMES = "Images names"
        const val INTENT_EXTRA_IMAGES_HREFS = "Images hrefs"
        const val INTENT_EXTRA_LIKES = "Images likes"

        const val REQUEST_CODE_SHOW = 1

    }

    private val tabListener = TabListener()

    private val pageListener = PageListener()

    /**
     *  List of listeners, each of which will be notified if like event occurs
     */
    private val onLikeListeners = ArrayList<OnLikeListener>()

    fun addOnLikeListener(listener: OnLikeListener): Boolean =
            onLikeListeners.add(listener)

    fun removeOnLikeListener(listener: OnLikeListener): Boolean =
            onLikeListeners.remove(listener)


    /**
     * References to fragments
     */
    private lateinit var recentFragment: RecentFragment
    private lateinit var topFragment: TopFragment
    private lateinit var favoriteFragment: FavoriteFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        (toolbar.layoutParams as AppBarLayout.LayoutParams).scrollFlags = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL.or(AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)
            }
            else -> {
                app_bar.setExpanded(false, false)
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
            }
        }

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        tabLayout.addOnTabSelectedListener(tabListener)

        // Just to make sure...
        System.setProperty("http.keepAlive", "true")

        view_pager.setOnPageChangeListener(pageListener)

        findOrCreateFragments()

        if (view_pager.adapter == null) {
            view_pager.adapter = GalleryPagerAdapter(supportFragmentManager)
            view_pager.offscreenPageLimit = 2
        }

    }

    /**
     *  Method that looks for already existing
     *  GalleryFragments
     *  in FragmentManager
     *
     *  Keeps references to fragments up-to-dated through all activity lifecycle's changes
     *
     */
    private fun findOrCreateFragments() {

        val recent = supportFragmentManager.findFragmentByTag(makeFragTag(0)) as RecentFragment?
        recentFragment = recent ?: RecentFragment()

        val top = supportFragmentManager.findFragmentByTag(makeFragTag(1)) as TopFragment?
        topFragment = top ?: TopFragment()

        val favorites = supportFragmentManager.findFragmentByTag(makeFragTag(2)) as FavoriteFragment?
        favoriteFragment = favorites ?: FavoriteFragment()
    }

    /**
     *  Function for imitating tags that FragmentPagerAdapter gives to the fragments
     *  when attaching them to activity.
     *  @see {FragmentPagerAdapter#makeFragmentName}
     */
    private fun makeFragTag(position: Int): String {
        return "android:switcher:${view_pager.id}:$position"
    }


    /**
     *  Adapter that attaches manages GalleryFragments to ViewPager
     */
    inner class GalleryPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {

            return when (position) {
                0 -> recentFragment
                1 -> topFragment
                else -> favoriteFragment

            }
        }

        override fun getCount(): Int {
            return 3
        }

    }


    /**
     *  Class that connects ViewPager with TabLayout
     */
    inner class PageListener : ViewPager.SimpleOnPageChangeListener() {

        override fun onPageSelected(position: Int) {

            tab_layout.getTabAt(position)?.select()

        }
    }

    inner class TabListener : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {

            view_pager.currentItem = tab?.position ?: 0

            val fragment = when (view_pager.currentItem) {
                0 -> recentFragment
                1 -> topFragment
                else -> favoriteFragment
            }

            invalidateFab(fragment)
        }

    }


    /**
     * Method that handles Scroll-To-Top FAB
     * @param fragment As this method is usually called from
     *  RecyclerView.OnChildAttachStateChangeListener#onChildViewDetachedFromWindow method,
     *  and each of the alive GalleryFragments contains RecyclerView
     *  that can be changed during the like events,
     *  we need to make sure that the calling fragment is in focus
     */
    fun invalidateFab(fragment: GalleryFragment) {

        val curFragment = when (view_pager.currentItem) {
            0 -> recentFragment
            1 -> topFragment
            else -> favoriteFragment
        }

        if (curFragment != fragment) {
            return
        }

        fab_up?.setOnClickListener {
            fragment.recycler?.scrollToPosition(0)
        }

        val flv = (curFragment.recycler?.layoutManager as GridLayoutManager?)?.findFirstCompletelyVisibleItemPosition()
                ?: return

        if (fab_up.visibility != View.VISIBLE && flv > 7) {

            animateFabVisibility(View.VISIBLE)

        } else if (flv < 4) {

            animateFabVisibility(View.GONE)
        }
    }

    var isFabAnimated = false

    private fun animateFabVisibility(visibility: Int) {

        if (isFabAnimated) {
            return
        }

        val animation: ViewPropertyAnimator?

        if (visibility == View.VISIBLE) {

            fab_up.alpha = 0f
            fab_up.translationY = 3 * fab_up.height.toFloat()

            fab_up.visibility = View.VISIBLE

            animation = fab_up.animate()
                    .alpha(1f)
                    .translationY(0f)

                    .setDuration(225)
                    .setInterpolator(FastOutSlowInInterpolator())

        } else {

            animation = fab_up.animate()

                    .alpha(0f)
                    .translationY(3 * fab_up.height.toFloat())

                    .setDuration(195)
                    .setInterpolator(FastOutLinearInInterpolator())

        }

        animation?.setListener(

                object : Animator.AnimatorListener {

                    override fun onAnimationRepeat(p0: Animator?) {}

                    override fun onAnimationEnd(p0: Animator?) {

                        if (visibility == View.GONE)
                            fab_up.visibility = View.GONE

                        fab_up.animate().setListener(null).start()

                        isFabAnimated = false
                    }

                    override fun onAnimationCancel(p0: Animator?) {

                        isFabAnimated = false
                    }

                    override fun onAnimationStart(p0: Animator?) {
                        isFabAnimated = true
                    }

                })?.start()
    }


    /**
     * Class that requests to invalidate Scroll-To-Top FAB as user scrolls through the GalleryFragment's Recycler
     */
    inner class FabInvalidator(private val fragment: GalleryFragment) : RecyclerView.OnChildAttachStateChangeListener {

        override fun onChildViewDetachedFromWindow(view: View?) {

            invalidateFab(fragment)
        }

        override fun onChildViewAttachedToWindow(view: View?) {
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode) {

            MainActivity.REQUEST_CODE_SHOW -> {

                if (resultCode != Activity.RESULT_OK)
                    return


                /**
                 * Receive info about like events which occurred during image viewing.
                 */
                val liked = data?.getBooleanArrayExtra(ImageActivity.INTENT_EXTRA_IS_FAVORITES)
                val imagesNames = data?.getStringArrayListExtra(INTENT_EXTRA_IMAGES_NAMES)

                if (liked?.size != imagesNames?.size || liked == null || imagesNames == null) {
                    return
                }

                for (i in 0 until liked.size) {
                    handleLikeEvent(imagesNames[i], liked[i])
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    /**
     * Interface for classes interested in like events
     */
    interface OnLikeListener {

        fun onLikeEvent(imageName: String?, isLiked: Boolean)
    }

    /**
     * Method that calls onLikeEvent in every interested class
     */
    fun handleLikeEvent(imageName: String?, isLiked: Boolean) {

        for (listener in onLikeListeners) {
            listener.onLikeEvent(imageName, isLiked)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.menu_item_refresh -> {

                for (i in 0 until 3) {

                    val fragment = when (view_pager.currentItem) {
                        0 -> recentFragment
                        1 -> topFragment
                        else -> favoriteFragment
                    }

                    if (!fragment.isLoading) {
                        fragment.loadImages()
                    }
                }

                true

            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
