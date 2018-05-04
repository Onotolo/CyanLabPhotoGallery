package comonotolo.httpsgithub.cyanlabphotogallery.fragments

import android.animation.Animator
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.R
import comonotolo.httpsgithub.cyanlabphotogallery.activities.ImageActivity
import kotlinx.android.synthetic.main.fragment_image.*
import java.io.File
import java.lang.Exception

class ImageFragment : Fragment() {

    companion object {

        val KEY_NAME = "name"
        val KEY_HREF = "href"
        val KEY_POISITION = "position"

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

    lateinit var imageHref: String
    lateinit var imageName: String
    var image: Bitmap? = null
    var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        imageHref = arguments?.getString(KEY_HREF) ?: ""
        imageName = arguments?.getString(KEY_NAME) ?: ""
        position = arguments?.getInt(KEY_POISITION) ?: 0

        super.onCreate(savedInstanceState)
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

        if ((favorites?.contains("${imageName.replace('.', '@')}.png") != true)) {

            Picasso.get().load("${imageHref}_XXL")
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .into(imageView, callback)

        } else {

            Picasso.get()
                    .load(File("${activity?.filesDir?.absolutePath}/${imageName.replace('.', '@')}.png"))
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .into(imageView, callback)

        }

        (activity as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)

        val detector = GestureDetector(activity, OnDoubleClickListener())

        imageView.setOnTouchListener { view, motionEvent ->

            detector.onTouchEvent(motionEvent)

            true
        }

        activity?.invalidateOptionsMenu()

        return view
    }

    inner class OnDoubleClickListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent?): Boolean {

            (activity as ImageActivity).animateLike(!(activity as ImageActivity).likes[position])

            return super.onDoubleTap(e)
        }

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

            return super.onFling(e1, e2, velocityX, velocityY)
        }

    }

    inner class RemoveAnimationListener() : Animator.AnimatorListener {

        override fun onAnimationEnd(p0: Animator?) {
            image_place.animate().setListener(null).start()

            (activity as ImageActivity).shareResults()
        }

        override fun onAnimationRepeat(p0: Animator?) {}

        override fun onAnimationCancel(p0: Animator?) {}

        override fun onAnimationStart(p0: Animator?) {}

    }


    fun prepareToDie() {

        (activity as ImageActivity).addResult((activity as ImageActivity).likes[position], position, image)

    }

    override fun onPause() {

        (activity as ImageActivity).aliveFragments.remove(this)

        prepareToDie()

        System.gc()

        super.onPause()
    }
}