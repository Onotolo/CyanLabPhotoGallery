package comonotolo.httpsgithub.cyanlabphotogallery.activities

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.ImageView
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.R
import kotlinx.android.synthetic.main.activity_image.*
import java.io.BufferedOutputStream
import java.io.File
import java.lang.Exception
import kotlin.concurrent.thread

class ImageActivity : AppCompatActivity() {

    companion object {
        val INTENT_EXTRA_IS_FAVORITE = "is Favorite"
    }

    var imageHref: String? = null

    var isFavorite = false

    var imageName: String? = null

    var imagePosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image)

        imageHref = intent.getStringExtra(MainActivity.INTENT_EXTRA_IMAGE_HREF)
        imageName = intent.getStringExtra(MainActivity.INTENT_EXTRA_IMAGE_NAME)
        imagePosition = intent.getIntExtra(MainActivity.INTENT_EXTRA_POSITION, -1)

        val imageView = findViewById<ImageView>(R.id.image_place)

        supportActionBar?.title = imageName

        val favorites = filesDir.list()

        val callback = object : Callback {

            override fun onSuccess() {
                progressBar.visibility = View.GONE
            }

            override fun onError(e: Exception?) {

            }

        }

        isFavorite = if (!favorites.contains("${imageName?.replace('.', '@')}.png")) {

            Picasso.get().load("${imageHref}_XXL").memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE).into(imageView, callback)

            false

        }else {

            Picasso.get().load(File("${filesDir.absolutePath}/${imageName?.replace('.', '@')}.png")).into(imageView, callback)

            true

        }

        supportActionBar?.setHomeButtonEnabled(true)

        val detector = GestureDetector(this, OnDoubleClickListener())

        imageView.setOnTouchListener { view, motionEvent ->

            detector.onTouchEvent(motionEvent)

            true
        }

    }

    inner class OnDoubleClickListener() : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent?): Boolean {

            animateLike(!isFavorite)

            return super.onDoubleTap(e)
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {

            val y2 = e2?.y
            val y1 = e1?.y

            if (y2 != null && y1 != null && y1 - y2 > 100){

                image_place.animate().translationY(-1000f).setDuration(195).alpha(0f).setListener(RemoveAnimationListener()).start()

            }

            return super.onFling(e1, e2, velocityX, velocityY)
        }



    }

    inner class RemoveAnimationListener() : Animator.AnimatorListener {

        override fun onAnimationEnd(p0: Animator?) {
            prepareToDie()
        }

        override fun onAnimationRepeat(p0: Animator?) {}

        override fun onAnimationCancel(p0: Animator?) {}

        override fun onAnimationStart(p0: Animator?) {}

    }

    fun animateLike(isLiked: Boolean?) {

        isFavorite = isLiked == true

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

    fun prepareToDie(){

        val favorites = filesDir.list()

        val name = imageName?.replace('.', '@')

        if (!isFavorite && favorites.contains("$name.png")) {
            thread {
                deleteFile("$name.png")
            }
        } else if (isFavorite && !favorites.contains("$name.png") && image_place.drawable != null) {

            thread {

                val image = (image_place.drawable as BitmapDrawable).bitmap

                val out = BufferedOutputStream(openFileOutput("$name.png", Context.MODE_PRIVATE))

                image.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

            }
        }

        if (image_place.drawable == null)
            isFavorite = false

        val data = Intent()

        data.putExtra(INTENT_EXTRA_IS_FAVORITE, isFavorite)
        data.putExtra(MainActivity.INTENT_EXTRA_POSITION, imagePosition)
        data.putExtra(MainActivity.INTENT_EXTRA_IMAGE_NAME, imageName)

        setResult(Activity.RESULT_OK, data)

        finish()

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        menu?.getItem(1)?.setIcon(if (isFavorite) R.drawable.ic_favorite_white_36dp else R.drawable.ic_favorite_border_white_36dp)

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.activity_image_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId){

            R.id.menu_item_like -> {

                animateLike(!isFavorite)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {

        prepareToDie()

        super.onBackPressed()
    }
}