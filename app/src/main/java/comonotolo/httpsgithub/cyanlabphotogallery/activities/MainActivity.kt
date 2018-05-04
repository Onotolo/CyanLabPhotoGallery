package comonotolo.httpsgithub.cyanlabphotogallery.activities

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
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


class MainActivity : AppCompatActivity() {

    companion object {

        val INTENT_EXTRA_POSITION = "Image position"
        val INTENT_EXTRA_IMAGES_NAMES = "Images names"
        val INTENT_EXTRA_IMAGES_HREFS = "Images hrefs"
        val INTENT_EXTRA_LIKES = "Images likes"

        val REQUEST_CODE_SHOW = 1

        var favoriteFragmentID = -1
        var topFragmentID = -1
        var recentFragmentID = -1

    }

    val tabListener = TabListener()

    val pageListener = PageListener()

    lateinit var recentFragment: RecentFragment
    lateinit var topFragment: TopFragment
    lateinit var favoriteFragment: FavoriteFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        tabLayout.addOnTabSelectedListener(tabListener)

        System.setProperty("http.keepAlive", "true")

        view_pager.setOnPageChangeListener(pageListener)

        val recent = supportFragmentManager.findFragmentByTag(makeFragTag(0)) as RecentFragment?
        recentFragment = recent ?: RecentFragment()

        val top = supportFragmentManager.findFragmentByTag(makeFragTag(1)) as TopFragment?
        topFragment = top ?: TopFragment()

        val favorites = supportFragmentManager.findFragmentByTag(makeFragTag(2)) as FavoriteFragment?
        favoriteFragment = favorites ?: FavoriteFragment()

        if (view_pager.adapter == null) {
            view_pager.adapter = PagerAdapter(supportFragmentManager)
            view_pager.offscreenPageLimit = 2
        }



    }

    inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {

            val fragment = supportFragmentManager.findFragmentByTag(makeFragTag(position))

            if (fragment != null)
                return fragment

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

    fun makeFragTag(position: Int): String {
        return "android:switcher:${view_pager.id}:$position"
    }

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

    var isFabAnimated = false

    fun animateFabVisibility(visibility: Int) {

        if (!isFabAnimated) {
            var animation: ViewPropertyAnimator? = null

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
    }

    inner class OnChildStateChangedLoader : RecyclerView.OnChildAttachStateChangeListener {

        override fun onChildViewDetachedFromWindow(view: View?) {

            val curFragment = when (view_pager.currentItem) {
                0 -> recentFragment
                1 -> topFragment
                else -> favoriteFragment
            }

            invalidateFab(curFragment)
        }

        override fun onChildViewAttachedToWindow(view: View?) {
        }

    }

    fun invalidateFab(fragment: GalleryFragment) {

        val curFragment = when (view_pager.currentItem) {
            0 -> recentFragment
            1 -> topFragment
            else -> favoriteFragment
        }

        if (curFragment == fragment) {
            val flv = (curFragment.recycler?.layoutManager as GridLayoutManager?)?.findFirstCompletelyVisibleItemPosition()
                    ?: return

            if (fab_up.visibility != View.VISIBLE && flv > 7) {

                animateFabVisibility(View.VISIBLE)
                fab_up?.setOnClickListener {
                    fragment.recycler?.scrollToPosition(0)
                }

            } else if (flv < 4) {

                animateFabVisibility(View.GONE)
            }


        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode){

            MainActivity.REQUEST_CODE_SHOW -> {

                if (resultCode == Activity.RESULT_OK){

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
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun handleLikeEvent(imageName: String?, isLiked: Boolean) {

        favoriteFragment.onLikeEvent(imageName, isLiked)
        recentFragment.onLikeEvent(imageName, isLiked)
        topFragment.onLikeEvent(imageName, isLiked)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {

            R.id.menu_item_refresh -> {

                val fragment = when (view_pager.currentItem) {
                    0 -> recentFragment
                    1 -> topFragment
                    else -> favoriteFragment
                }

                if (!fragment.isLoading) {
                    fragment.loadImages()
                    true
                } else
                    false

            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
