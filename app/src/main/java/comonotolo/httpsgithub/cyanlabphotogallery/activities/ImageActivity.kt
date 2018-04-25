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
import android.view.*
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

                activity.downloadLiked()
            }

            return super.onDoubleTap(e)
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return super.onDown(e)
        }
    }

    fun downloadLiked(){
        val href = imageHref

        val image = image

        if (href != null && image != null){

            val imageIndex = MainActivity.imagesHrefs.indexOf(href)

            val smallImage = MainActivity.images[imageIndex]

            thread {
                val out = BufferedOutputStream(openFileOutput("${href.replace('/', '@')}.png", Context.MODE_PRIVATE))

                image.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

                val outSmall = BufferedOutputStream(openFileOutput("${href.replace('/', '@')}@small.png", Context.MODE_PRIVATE))

                smallImage?.compress(Bitmap.CompressFormat.PNG, 100, outSmall)
                outSmall.flush()
                outSmall.close()
            }

            like.visibility = View.VISIBLE

            like.scaleX = 0f
            like.scaleY = 0f

            like.alpha = 0f

            like.animate()
                    .alpha(1f)
                    .scaleX(3f).scaleY(3f)
                    .setInterpolator(FastOutSlowInInterpolator())
                    .setDuration(225)
                    .setListener(LikeAnimationListener(this))
                    .start()


            isFavorite = true

            invalidateOptionsMenu()
        }
    }

    fun dislike(){

        isFavorite = false

        deleteFile("${imageHref?.replace('/', '@')}.png")
        deleteFile("${imageHref?.replace('/', '@')}@small.png")

        invalidateOptionsMenu()
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

    fun prepareToDie(){

        val data = Intent()

        data.putExtra(INTENT_EXTRA_IS_FAVORITE, isFavorite)

        setResult(Activity.RESULT_OK, data)

        finish()

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        menu?.getItem(0)?.setIcon(if (isFavorite) R.drawable.ic_favorite_white_36dp else R.drawable.ic_favorite_border_white_36dp)

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.activity_item_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId){
            R.id.menu_item_like -> {
                if (!isFavorite){
                    downloadLiked()
                }else
                    dislike()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {

        prepareToDie()

        super.onBackPressed()
    }
}