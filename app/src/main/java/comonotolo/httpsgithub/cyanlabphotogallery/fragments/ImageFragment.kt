package comonotolo.httpsgithub.cyanlabphotogallery.fragments

import android.animation.Animator
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.R
import comonotolo.httpsgithub.cyanlabphotogallery.activities.ImageActivity
import kotlinx.android.synthetic.main.fragment_image.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import kotlin.concurrent.thread


/**
 * Fragment with one image.
 *
 * Is used in ImageActivity inside of ViewPager
 * Has a very strong cohesion with ImageActivity
 */
class ImageFragment : Fragment() {

    companion object {

        const val KEY_NAME = "name"
        const val KEY_HREF = "href"
        const val KEY_POISITION = "position"


        /**
         * Fabric function for creating new instances of ImageFragment
         */
        fun getNewInstance(imageName: String?, imageHref: String?, position: Int): ImageFragment {

            val fragment = ImageFragment()

            val data = Bundle()
            data.putString(KEY_NAME, imageName)
            data.putString(KEY_HREF, imageHref)
            data.putInt(KEY_POISITION, position)

            fragment.arguments = data

            return fragment
        }
    }

    private lateinit var imageHref: String
    private lateinit var imageName: String
    var image: Bitmap? = null
    var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageHref = arguments?.getString(KEY_HREF) ?: ""
        imageName = arguments?.getString(KEY_NAME) ?: ""
        position = arguments?.getInt(KEY_POISITION) ?: 0

        setHasOptionsMenu(true)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_image, container, false)

        val imageView = view.findViewById<ImageView>(R.id.image_place)

        val favorites = activity?.filesDir?.list()

        val callback = object : Callback {

            override fun onSuccess() {

                view.findViewById<ProgressBar>(R.id.progressBar)?.visibility = View.GONE
                image = (imageView.drawable as BitmapDrawable).bitmap
            }

            override fun onError(e: Exception?) {

            }

        }


        /**
         * If image is favorite we don't need to download it from net
         */
        if ((favorites?.contains("${imageName.replace('.', '@')}.png") != true)) {

            Picasso.get().load("${imageHref}_XXL")
                    .into(imageView, callback)

        } else {

            Picasso.get()
                    .load(File("${activity?.filesDir?.absolutePath}/${imageName.replace('.', '@')}.png"))
                    .into(imageView, callback)

        }

        (activity as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)

        val detector = GestureDetector(activity, OnImageGestureListener())

        imageView.setOnTouchListener { view, motionEvent ->

            detector.onTouchEvent(motionEvent)

            true
        }

        activity?.invalidateOptionsMenu()

        return view
    }

    inner class OnImageGestureListener : GestureDetector.SimpleOnGestureListener() {


        /**
         * User can add the picture to favorites by double tapping it
         */
        override fun onDoubleTap(e: MotionEvent?): Boolean {

            (activity as ImageActivity).animateLike(!(activity as ImageActivity).likes[position])

            return super.onDoubleTap(e)
        }

        /**
         * User can swipe the image from bottom to top to close ImageActivity
         */
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {

            val y2 = e2?.y
            val y1 = e1?.y

            if (y2 != null && y1 != null && y1 - y2 > 100) {

                image_place.animate().apply {
                    translationY(-1000f)
                    duration = 195
                    alpha(0f)
                    setListener(RemoveAnimationListener())
                    start()
                }

            }

            return true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        inflater?.inflate(R.menu.fragment_image_menu, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private var isSharing = false

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {

            R.id.menu_item_share -> {

                if (isSharing)
                    return false

                /**
                 * The simplest version of sharing image with saving it into internal cache
                 * It will be removed from cache when ImageActivity is closed
                 */
                thread {
                    try {
                        isSharing = true

                        val file = File(activity?.cacheDir, "sharedPic.png")

                        val out = BufferedOutputStream(FileOutputStream(file))

                        image?.compress(Bitmap.CompressFormat.PNG, 100, out)

                        out.flush()
                        out.close()

                        file.setReadable(true, false)

                        val bitmapUri = Uri.fromFile(file)

                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "image/*"
                        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
                        activity?.startActivity(Intent.createChooser(intent, "Share"))

                    } catch (ex: IOException) {

                        ex.printStackTrace()

                    } finally {
                        isSharing = false

                    }
                }
                return true
            }

        }

        return false
    }

    inner class RemoveAnimationListener : Animator.AnimatorListener {

        override fun onAnimationEnd(p0: Animator?) {
            image_place.animate().setListener(null).start()

            (activity as ImageActivity).prepareEvents()
        }

        override fun onAnimationRepeat(p0: Animator?) {}

        override fun onAnimationCancel(p0: Animator?) {}

        override fun onAnimationStart(p0: Animator?) {}

    }


    /**
     * Making ImageActivity know that this image has been liked or disliked
     */
    private fun prepareToDie() {

        (activity as ImageActivity).addLikeEvent((activity as ImageActivity).likes[position], position, image)

    }

    override fun onPause() {

        /**
         * Removing destroyed fragment from ImageActivity's alive fragments list
         */
        (activity as ImageActivity).aliveFragments.remove(this)

        prepareToDie()

        System.gc()

        super.onPause()
    }
}