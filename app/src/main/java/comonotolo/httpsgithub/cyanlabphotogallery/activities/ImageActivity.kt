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
import java.io.File

/**
 * Activity, that allows user to view pictures separately
 * Uses View Pager with FragmentStatePagerAdapter, presenting each image on separate fragment
 *
 * Here and throughout all application the terms Like Event and Favorite Event are equal and refer to user's adding or removing picture from Favorites*/
class ImageActivity : AppCompatActivity() {

    companion object {
        const val INTENT_EXTRA_IS_FAVORITES = "is Favorites"
    }

    /**
     * Position of currently visible image
     */
    private var imagePosition = -1

    // Some kind of stack holding all like events occurred during activity's life
    private val likesStack = ArrayList<Boolean>()
    private val imagesNamesStack = ArrayList<String?>()

    lateinit var imagesHrefs: ArrayList<String?>
    lateinit var imagesNames: ArrayList<String?>
    lateinit var likes: BooleanArray

    /**
     * List of alive fragments in the View Pager.
     *
     * It is needed when activity finishes and we need to get like events from these fragments
     * as like events are usually handled from fragment to activity when user proceeds through images and fragment is destroyed by FragmentStatePagerAdapter.
     *
     *  If fragment is destroyed because of user's going further through images, it will be removed from this list.
     */
    val aliveFragments = ArrayList<ImageFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_images)

        imagesHrefs = intent.getStringArrayListExtra(MainActivity.INTENT_EXTRA_IMAGES_HREFS)
        imagesNames = intent.getStringArrayListExtra(MainActivity.INTENT_EXTRA_IMAGES_NAMES)
        imagePosition = intent.getIntExtra(MainActivity.INTENT_EXTRA_POSITION, 0)
        likes = intent.getBooleanArrayExtra(MainActivity.INTENT_EXTRA_LIKES)

        supportActionBar?.title = imagesNames[imagePosition]
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pager.adapter = ImagesAdapter(supportFragmentManager)

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

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

            // Adding new fragment to list.
                aliveFragments.add(fragment)

            return fragment
        }

        override fun getCount(): Int = imagesNames.size

    }


    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        //Refreshing like icon state due to picture
        menu?.getItem(0)?.setIcon(
                if (likes[pager.currentItem]) {
                    R.drawable.ic_favorite_dark_36dp
                } else R.drawable.ic_favorite_border_dark_36dp)

        return true
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

        like.setImageResource(if (isLiked == true) R.drawable.ic_favorite_white_36dp else R.drawable.ic_favorite_border_white_36dp)

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

    inner class LikeAnimationListener : Animator.AnimatorListener {
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

        onBackPressed()

        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        // Clear the cache
        val file = File(cacheDir, "sharedPic.png")
        file.delete()

    }

    override fun onBackPressed() {

        prepareEvents()

        super.onBackPressed()
    }

    /**
     * Handling like event and adding it to the stack to notify MainActivity when finished
     *
     * Note:
     * Like events are added not every time the user taps like icon or double taps the image,
     * but only when fragment with image is destroyed or activity is finishing
     * and some changes occurred: e.g. if user likes and then dislikes image, imageEvent wont be added to stack.
     */
    fun addLikeEvent(isLiked: Boolean?, position: Int, bitmap: Bitmap?) {

        val isChanged = FavoritesManager(this).handleLikeEvent(imagesNames[position], isLiked == true, bitmap)

        if (!isChanged) {
            return
        }

        val imageName = imagesNames[position]

        if (imagesNamesStack.contains(imageName)) {

            likesStack[imagesNamesStack.indexOf(imageName)] = (isLiked == true)
            return
        }

        imagesNamesStack.add(imageName)
        likesStack.add(isLiked == true)

        return
    }

    // All results are passed to calling activity via Intent
    fun prepareEvents() {

        for (fragment in aliveFragments) {

            addLikeEvent(likes[fragment.position], fragment.position, fragment.image)
        }

        val data = Intent()

        data.putExtra(ImageActivity.INTENT_EXTRA_IS_FAVORITES, likesStack.toBooleanArray())
        data.putExtra(MainActivity.INTENT_EXTRA_IMAGES_NAMES, imagesNamesStack)

        setResult(Activity.RESULT_OK, data)

        finish()
    }


}