package comonotolo.httpsgithub.cyanlabphotogallery.view

import android.animation.Animator
import android.content.Intent
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.activities.ImageActivity
import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.GalleryFragment
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.NetFragment
import comonotolo.httpsgithub.cyanlabphotogallery.model.FavoritesManager
import kotlinx.android.synthetic.main.image_holder.view.*
import kotlin.concurrent.thread

/**
 * Class which represents single image in gallery's Recycler View
 */
class ImageHolder(val fragment: GalleryFragment, imageView: View) : RecyclerView.ViewHolder(imageView) {

    val activity = fragment.activity as MainActivity
    val image = imageView.holder_image
    val like = imageView.holder_like
    val progressLike = imageView.progress_like

    init {

        val detector = GestureDetector(activity, OnImageGestureListener())

        imageView.setOnTouchListener { _, motionEvent ->

            detector.onTouchEvent(motionEvent)

            true
        }
    }


    /**
     *  User can double tap the image to add it to the Favorites.
     *  If a single tap is confirmed, ImageActivity will be started
     */
    inner class OnImageGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent?): Boolean {

            if (fragment is NetFragment)
                animateLike(!fragment.likeFlags[adapterPosition])

            return true
        }


        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {

            val data = Intent(activity, ImageActivity::class.java)

            data.putExtra(MainActivity.INTENT_EXTRA_POSITION, adapterPosition)
                    .putExtra(MainActivity.INTENT_EXTRA_IMAGES_NAMES, fragment.imagesNames)
                    .putExtra(MainActivity.INTENT_EXTRA_IMAGES_HREFS, fragment.imagesHrefs)
                    .putExtra(MainActivity.INTENT_EXTRA_LIKES,
                            if (fragment.mode != GalleryFragment.MODE_FAVORITES) fragment.likeFlags.toBooleanArray()
                            else BooleanArray(fragment.imagesNames.size, { true })
                    )

            activity.startActivityForResult(data, MainActivity.REQUEST_CODE_SHOW)

            return true
        }

    }

    /**
     * Method that animates like event that occurs when user double-taps image in the Gallery
     */
    fun animateLike(isLiked: Boolean) {

        if (!isLiked) {

            (fragment as NetFragment).likeFlags[adapterPosition] = isLiked
            FavoritesManager(activity).handleLikeEvent(fragment.imagesNames[adapterPosition], isLiked, null)
            activity.handleLikeEvent(fragment.imagesNames[adapterPosition], isLiked)

            like.animate().apply {
                alpha(0f)
                scaleX(0f).scaleY(0f)
                interpolator = LinearOutSlowInInterpolator()
                duration = 165
                setListener(LikeAnimationListener())
                start()
            }

            return

        }

        progressLike.visibility = View.VISIBLE

        like.scaleX = 0f
        like.scaleY = 0f

        like.alpha = 0f


        /**
         * This block of code might has been better if it's been wrapped it into Kotlin's coroutine instead of thread as it blocks until image is fully downloaded.
         */
        thread {

            val bitmap = Picasso.get().load("${fragment.imagesHrefs[adapterPosition]}_XXL")
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .get()

            (fragment as NetFragment).likeFlags[adapterPosition] = isLiked

            val manager = FavoritesManager(fragment.activity)

            manager.handleLikeEvent(fragment.imagesNames[adapterPosition], isLiked, bitmap)

            while (manager.isInProcess) {
            }

            activity.runOnUiThread {

                activity.handleLikeEvent(fragment.imagesNames[adapterPosition], isLiked)

                progressLike.visibility = View.GONE

                like.visibility = View.VISIBLE

                like.animate().apply {
                    alpha(1f)
                    scaleX(1.5f).scaleY(1.5f)
                    interpolator = FastOutSlowInInterpolator()
                    duration = 175
                    setListener(LikeAnimationListener())
                    start()
                }
            }
        }

    }

    inner class LikeAnimationListener : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}

        override fun onAnimationEnd(p0: Animator?) {

            if (!fragment.likeFlags[adapterPosition]) {

                like.visibility = View.GONE
                return
            }

            like.animate()
                    .alpha(1f)
                    .scaleX(1f).scaleY(1f)
                    .setInterpolator(LinearOutSlowInInterpolator())
                    .setDuration(155)
                    .setListener(null)
                    .start()

        }

        override fun onAnimationCancel(p0: Animator?) {}

        override fun onAnimationStart(p0: Animator?) {}

    }

}