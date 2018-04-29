package comonotolo.httpsgithub.cyanlabphotogallery.view

import android.animation.Animator
import android.content.Intent
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.widget.RecyclerView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.activities.ImageActivity
import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.GalleryFragment
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.NetFragment
import comonotolo.httpsgithub.cyanlabphotogallery.model.FavoritesManager
import kotlinx.android.synthetic.main.image_holder.view.*
import kotlin.concurrent.thread

class ImageHolder(val fragment: GalleryFragment, imageView: View) : RecyclerView.ViewHolder(imageView) {

    val image = imageView.holder_image
    val like = imageView.holder_like

    init {

        val detector = GestureDetector(fragment.activity, OnDoubleClickListener())

        imageView.setOnTouchListener(View.OnTouchListener { p0, p1 ->

            detector.onTouchEvent(p1)

            true
        })
    }


    inner class OnDoubleClickListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent?): Boolean {

            if (fragment is NetFragment)
                animateLike(!fragment.likeFlags[adapterPosition])

            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {

            val data = Intent(fragment.activity, ImageActivity::class.java)

            data.putExtra(MainActivity.INTENT_EXTRA_POSITION, adapterPosition)
                    .putExtra(MainActivity.INTENT_EXTRA_IMAGES_NAMES, fragment.imagesNames)
                    .putExtra(MainActivity.INTENT_EXTRA_IMAGES_HREFS, fragment.imagesHrefs)
                    .putExtra(MainActivity.INTENT_EXTRA_LIKES,
                            if (fragment.mode != GalleryFragment.MODE_FAVORITES) fragment.likeFlags.toBooleanArray()
                            else BooleanArray(fragment.imagesNames.size, { true })
                    )

            fragment.activity?.startActivityForResult(data, MainActivity.REQUEST_CODE_SHOW)

            return true
        }

    }

    fun animateLike(isLiked: Boolean) {

        (fragment as NetFragment).likeFlags[adapterPosition] = isLiked

        if (isLiked) {
            like.scaleX = 0f
            like.scaleY = 0f

            like.alpha = 0f

            like.visibility = View.VISIBLE

            like.animate()
                    .alpha(1f)
                    .scaleX(1.5f).scaleY(1.5f)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .setDuration(175)
                    .setListener(LikeAnimationListener())
                    .start()

        } else {

            like.animate()
                    .alpha(0f)
                    .scaleX(0f).scaleY(0f)
                    .setInterpolator(LinearOutSlowInInterpolator())
                    .setDuration(165)
                    .setListener(LikeAnimationListener())
                    .start()
        }




    }

    inner class LikeAnimationListener() : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}

        override fun onAnimationEnd(p0: Animator?) {

            val activity = fragment.activity as MainActivity

            if (fragment.likeFlags[adapterPosition]) {
                like.animate()
                        .alpha(1f)
                        .scaleX(1f).scaleY(1f)
                        .setInterpolator(LinearOutSlowInInterpolator())
                        .setDuration(95)
                        .setListener(null)
                        .start()
            } else {
                like.visibility = View.GONE
            }

            thread {
                val bitmap = Picasso.get().load("${fragment.imagesHrefs[adapterPosition]}_XXL").get()
                while (bitmap.byteCount <= 0) {
                }
                FavoritesManager(activity).handleLikeEvent(fragment.imagesNames[adapterPosition], fragment.likeFlags[adapterPosition], bitmap)

            }

            activity.handleLikeEvent(fragment.imagesNames[adapterPosition], fragment.likeFlags[adapterPosition])
        }

        override fun onAnimationCancel(p0: Animator?) {}

        override fun onAnimationStart(p0: Animator?) {}

    }

}