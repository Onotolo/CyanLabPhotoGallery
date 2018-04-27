package comonotolo.httpsgithub.cyanlabphotogallery.activities

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.ImageView
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.R
import kotlinx.android.synthetic.main.activity_image.*
import java.io.BufferedOutputStream
import java.io.File
import kotlin.concurrent.thread

class ImageActivity : AppCompatActivity(), View.OnClickListener {

    override fun onClick(p0: View?) {
        if (p0?.id == R.id.parent){
            prepareToDie()
        }
    }

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

        findViewById<ConstraintLayout>(R.id.parent).setOnClickListener(this)

        val favoritesHrefs = filesDir.list()

        supportActionBar?.title = MainActivity.imageName

        if (!favoritesHrefs.contains("${supportActionBar?.title}.png")) {

            Picasso.get().load( "${imageHref}_XXL" ).into(imageView)

            isFavorite = false

        }else {

            Picasso.get().load(File("${filesDir.absolutePath}/${MainActivity.imageName}.png")).into(imageView)

            isFavorite  = true

        }

        supportActionBar?.setHomeButtonEnabled(true)

        val detector = GestureDetector(this, OnDoubleClickListener(this))

        imageView.setOnTouchListener { view, motionEvent ->

            detector.onTouchEvent(motionEvent)

            true
        }

    }

    class OnDoubleClickListener(val activity: ImageActivity): GestureDetector.SimpleOnGestureListener(){

        override fun onDoubleTap(e: MotionEvent?): Boolean {

            if (!activity.isFavorite){

                activity.downloadLiked()
            } else{
                activity.dislike()
            }

            return super.onDoubleTap(e)
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {

            val y2 = e2?.y
            val y1 = e1?.y

            if (y2 != null && y1 != null && y1 - y2 > 100){

                activity.image_place.animate().translationY(-1000f).setDuration(195).alpha(0.2f).setListener(RemoveAnimationListener(activity)).start()

            }

            return super.onFling(e1, e2, velocityX, velocityY)
        }



    }

    class RemoveAnimationListener(val activity: ImageActivity): Animator.AnimatorListener{
        override fun onAnimationRepeat(p0: Animator?) {
        }

        override fun onAnimationEnd(p0: Animator?) {
            activity.prepareToDie()
        }

        override fun onAnimationCancel(p0: Animator?) {

        }

        override fun onAnimationStart(p0: Animator?) {

        }

    }

    fun downloadLiked(){
        val name = supportActionBar?.title

        val image = (image_place.drawable as BitmapDrawable).bitmap

        if (name != null && image != null) {

            val smallImage = MainActivity.bitmapAtPosition

            thread {
                val out = BufferedOutputStream(openFileOutput("$name.png", Context.MODE_PRIVATE))

                image.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

                val outSmall = BufferedOutputStream(openFileOutput("${name}_small.png", Context.MODE_PRIVATE))

                smallImage?.compress(Bitmap.CompressFormat.PNG, 100, outSmall)
                outSmall.flush()
                outSmall.close()
            }

            animateLike(true)

            isFavorite = true

            invalidateOptionsMenu()
        }
    }

    fun animateLike(isLiked: Boolean?){

        like.setImageResource( if (isLiked == true) R.drawable.ic_favorite_white_36dp else R.drawable.ic_favorite_border_white_36dp)

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
    }

    fun dislike(){

        isFavorite = false

        animateLike(false)

        thread {
            deleteFile("${supportActionBar?.title}.png")
            deleteFile("${supportActionBar?.title}_small.png")
        }

        invalidateOptionsMenu()

    }

    class LikeAnimationListener(val activity: ImageActivity): Animator.AnimatorListener{
        override fun onAnimationRepeat(p0: Animator?) {

        }

        override fun onAnimationEnd(p0: Animator?) {
            activity.like.animate()
                    .alpha(0f)
                    .scaleX(0f).scaleY(0f)
                    .setInterpolator(LinearOutSlowInInterpolator())
                    .setDuration(195).setStartDelay(225)
                    .setListener(null)
                    .start()
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