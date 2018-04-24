package comonotolo.httpsgithub.cyanlabphotogallery.activities

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.R
import kotlinx.android.synthetic.main.activity_image.*
import java.io.*
import kotlin.concurrent.thread

class ImageActivity : AppCompatActivity() {

    companion object {
        val INTENT_EXTRA_IS_FAVORITE = "is Favorite"
    }

    var imageHref: String? = null

    var isFavorite = false

    var image: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image)

        imageHref = intent.getStringExtra(MainActivity.INTENT_EXTRA_IMAGE_HREF)

        val imageView = findViewById<ImageView>(R.id.image_place)

        val favoritesHrefs = filesDir.list()

        if (!favoritesHrefs.contains("${imageHref?.replace('/', '@')}.png")) {

            val load = thread {
                image = Picasso.get().load( "${imageHref}_XXL" ).get()
            }

            load.join()
            while (image == null){}
            imageView.setImageBitmap(image)

            isFavorite = false

        }else {

            val load = thread {
                image = Picasso.get().load( File("${filesDir.absolutePath}/${imageHref?.replace('/', '@')}.png")).get()
            }

            load.join()
            while (image == null){}
            imageView.setImageBitmap(image)

            isFavorite  = true

        }

        val detector = GestureDetector(this, OnDoubleClickListener(this))

        imageView.setOnTouchListener { view, motionEvent ->

            if (!isFavorite)
                detector.onTouchEvent(motionEvent)

            true
        }

    }

    class OnDoubleClickListener(val activity: ImageActivity): GestureDetector.SimpleOnGestureListener(){

        override fun onDoubleTap(e: MotionEvent?): Boolean {

            if (!activity.isFavorite){

                val href = activity.imageHref

                val image = activity.image

                if (href != null && image != null){

                    val imageIndex = MainActivity.imagesHrefs.indexOf(href)

                    val smallImage = MainActivity.images[imageIndex]

                    thread {
                        val out = BufferedOutputStream(activity.openFileOutput("${href.replace('/', '@')}.png", Context.MODE_PRIVATE))

                        image.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                        out.close()

                        val outSmall = BufferedOutputStream(activity.openFileOutput("${href.replace('/', '@')}@small.png", Context.MODE_PRIVATE))

                        smallImage?.compress(Bitmap.CompressFormat.PNG, 100, outSmall)
                        outSmall.flush()
                        outSmall.close()
                    }

                    activity.like.visibility = View.VISIBLE

                    activity.like.scaleX = 0f
                    activity.like.scaleY = 0f

                    activity.like.alpha = 0f

                    activity.like.animate()
                            .alpha(1f)
                            .scaleX(1f).scaleY(1f)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .setDuration(225)
                            .setListener(LikeAnimationListener(activity))
                            .start()


                    activity.isFavorite = true
                }
            }

            return super.onDoubleTap(e)
        }

        class LikeAnimationListener(val activity: ImageActivity): Animator.AnimatorListener{
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                activity.like.animate().alpha(0f).scaleX(0f).scaleY(0f).setInterpolator(LinearOutSlowInInterpolator()).setDuration(195).setStartDelay(225).start()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationStart(p0: Animator?) {

            }

        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return super.onDown(e)
        }
    }

    fun prepareToDie(){

        val data = Intent()

        data.putExtra(INTENT_EXTRA_IS_FAVORITE, isFavorite)

        setResult(Activity.RESULT_OK, data)

        finish()

    }

    override fun onBackPressed() {

        prepareToDie()

        super.onBackPressed()
    }
}