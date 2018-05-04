package comonotolo.httpsgithub.cyanlabphotogallery.activities

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import comonotolo.httpsgithub.cyanlabphotogallery.R
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.ImageFragment
import comonotolo.httpsgithub.cyanlabphotogallery.model.FavoritesManager
import kotlinx.android.synthetic.main.activity_images.*

class ImageActivity : AppCompatActivity() {

    companion object {
        val INTENT_EXTRA_IS_FAVORITES = "is Favorites"
    }

    var imagePosition = -1

    var isLiked = false

    var curFragment: Fragment? = null

    val isFavorites = ArrayList<Boolean>()
    val changedImagesNames = ArrayList<String?>()

    lateinit var imagesHrefs: ArrayList<String?>
    lateinit var imagesNames: ArrayList<String?>
    lateinit var likes: BooleanArray

    val aliveFragments = ArrayList<ImageFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_images)

        imagesHrefs = intent.getStringArrayListExtra(MainActivity.INTENT_EXTRA_IMAGES_HREFS)
        imagesNames = intent.getStringArrayListExtra(MainActivity.INTENT_EXTRA_IMAGES_NAMES)
        imagePosition = intent.getIntExtra(MainActivity.INTENT_EXTRA_POSITION, 0)
        likes = intent.getBooleanArrayExtra(MainActivity.INTENT_EXTRA_LIKES)

        supportActionBar?.title = imagesNames[imagePosition]

        pager.adapter = ImagesAdapter(supportFragmentManager)

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                supportActionBar?.title = imagesNames[position]
                invalidateOptionsMenu()
            }

        })

        pager.currentItem = imagePosition

    }

    inner class ImagesAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {

            val fragment = ImageFragment
                    .getNewInstance(if (imagesNames.size > position) imagesNames[position] else null
                            , if (imagesHrefs.size > position) imagesHrefs[position] else null, position
                    )

            if (!aliveFragments.contains(fragment))
                aliveFragments.add(fragment)

            return fragment
        }

        override fun getCount(): Int = imagesNames.size

    }


    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        menu?.getItem(1)?.setIcon(
                if (likes[pager.currentItem]) R.drawable.ic_favorite_dark_36dp else R.drawable.ic_favorite_border_dark_36dp)

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.activity_image_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {

            R.id.menu_item_like -> {

                animateLike(!likes[pager.currentItem])
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun animateLike(isLiked: Boolean?) {

        likes[pager.currentItem] = isLiked == true

        invalidateOptionsMenu()

        like.setImageResource( if (isLiked == true) R.drawable.ic_favorite_white_36dp else R.drawable.ic_favorite_border_white_36dp)

        like.visibility = View.VISIBLE

        like.scaleX = 0f
        like.scaleY = 0f

        like.alpha = 0f

        like.animate()
                .alpha(1f)
                .scaleX(2f).scaleY(2f)
                .setInterpolator(FastOutSlowInInterpolator())
                .setDuration(225)
                .setListener(LikeAnimationListener())
                .start()
    }

    inner class LikeAnimationListener() : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}

        override fun onAnimationEnd(p0: Animator?) {
            like.animate()
                    .alpha(0f)
                    .scaleX(0f).scaleY(0f)
                    .setInterpolator(LinearOutSlowInInterpolator())
                    .setDuration(195).setStartDelay(225)
                    .setListener(null)
                    .start()
        }

        override fun onAnimationCancel(p0: Animator?) {}

        override fun onAnimationStart(p0: Animator?) {}

    }

    override fun onSupportNavigateUp(): Boolean {

        super.onBackPressed()

        return true
    }

    override fun onBackPressed() {

        shareResults()

        super.onBackPressed()
    }

    fun addResult(isLiked: Boolean?, position: Int, bitmap: Bitmap?) {

        val isChanged = FavoritesManager(this).handleLikeEvent(imagesNames[position], isLiked == true, bitmap)

        if (!isChanged) {
            return
        }

        val imageName = imagesNames[position]

        if (changedImagesNames.contains(imageName)) {

            isFavorites[changedImagesNames.indexOf(imageName)] = (isLiked == true)
            return
        }

        changedImagesNames.add(imageName)
        isFavorites.add(isLiked == true)
    }

    fun shareResults() {

        for (fragment in aliveFragments) {

            addResult(likes[fragment.position], fragment.position, fragment.image)
        }

        val data = Intent()

        data.putExtra(ImageActivity.INTENT_EXTRA_IS_FAVORITES, isFavorites.toBooleanArray())
        data.putExtra(MainActivity.INTENT_EXTRA_IMAGES_NAMES, changedImagesNames)

        setResult(Activity.RESULT_OK, data)

        finish()
    }

}