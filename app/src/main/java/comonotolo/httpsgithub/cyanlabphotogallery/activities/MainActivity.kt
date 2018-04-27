package comonotolo.httpsgithub.cyanlabphotogallery.activities

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
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
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.RecentFragment
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.TopFragment
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {

        val MODE_RECENT = R.string.mode_recent
        val MODE_TOP = R.string.mode_top
        val MODE_FAVORITES = R.string.mode_favorites

        val INTENT_EXTRA_IMAGE_HREF = "Image href"

        var mode = MainActivity.MODE_RECENT

        val REQUEST_CODE_SHOW = 1

        var imagePosition = 0
        var imageName: String? = null
        var bitmapAtPosition: Bitmap? = null

    }

    val tabListener = TabListener()

    val pageListener = PageListener()

    val recentFragment = RecentFragment()
    val topFragment = TopFragment()
    val favoriteFragment = FavoriteFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        tabLayout.addOnTabSelectedListener(tabListener)

        view_pager.setOnPageChangeListener(pageListener)

        view_pager.adapter = PagerAdapter(supportFragmentManager)

    }

    inner class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

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

            mode = when (view_pager.currentItem) {
                0 -> MODE_RECENT
                1 -> MODE_TOP
                else -> MODE_FAVORITES
            }

            val fragment = when (mode) {
                MODE_TOP -> topFragment
                MODE_RECENT -> recentFragment
                else -> favoriteFragment
            }

            val fvp = (fragment.recycler?.layoutManager as GridLayoutManager).findFirstCompletelyVisibleItemPosition()

            if (fab_up?.visibility == View.VISIBLE && fvp < 4) {

                animateFabVisibility(View.GONE)

            } else if (fab_up?.visibility == View.GONE && fvp > 7) {

                animateFabVisibility(View.VISIBLE)

            }

            fab_up?.setOnClickListener {
                fragment.recycler?.scrollToPosition(0)
            }

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

                        override fun onAnimationCancel(p0: Animator?) {}

                        override fun onAnimationStart(p0: Animator?) {
                            isFabAnimated = true
                        }

                    })?.start()
        }
    }

    inner class OnChildStateChangedLoader(val manager: GridLayoutManager) : RecyclerView.OnChildAttachStateChangeListener {

        override fun onChildViewDetachedFromWindow(view: View?) {

            val flv = manager.findFirstCompletelyVisibleItemPosition()

            if (fab_up.visibility != View.VISIBLE && flv > 7) {

                animateFabVisibility(View.VISIBLE)

            } else if (flv < 4) {

                animateFabVisibility(View.GONE)
            }

        }

        override fun onChildViewAttachedToWindow(view: View?) {
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode){

            MainActivity.REQUEST_CODE_SHOW -> {
                if (resultCode == Activity.RESULT_OK){

                    val fragment = when (mode) {
                        MODE_TOP -> topFragment
                        MODE_RECENT -> recentFragment
                        else -> favoriteFragment
                    }

                    if (MainActivity.mode != MainActivity.MODE_FAVORITES) {

                        fragment.likeFlags[MainActivity.imagePosition] = data?.getBooleanExtra(ImageActivity.INTENT_EXTRA_IS_FAVORITE, false) == true

                        fragment.recycler?.adapter?.notifyItemChanged(MainActivity.imagePosition)

                        if (fragment.likeFlags[imagePosition]) {

                            favoriteFragment.imagesNames.add(imageName)
                            favoriteFragment.recycler?.adapter?.notifyItemInserted(favoriteFragment.imagesNames.lastIndex)

                        } else if (favoriteFragment.imagesNames.contains(imageName)) {

                            val index = favoriteFragment.imagesNames.indexOf(imageName)

                            favoriteFragment.imagesNames.remove(imageName)
                            favoriteFragment.recycler?.adapter?.notifyItemRemoved(index)
                        }

                    }
                    else if (data?.getBooleanExtra(ImageActivity.INTENT_EXTRA_IS_FAVORITE, false) != true){

                        fragment.imagesNames.removeAt(MainActivity.imagePosition)

                        fragment.recycler?.adapter?.notifyItemRemoved(MainActivity.imagePosition)
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

}
