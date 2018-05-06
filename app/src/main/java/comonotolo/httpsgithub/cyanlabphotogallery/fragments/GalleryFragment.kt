package comonotolo.httpsgithub.cyanlabphotogallery.fragments

import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.R
import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity
import comonotolo.httpsgithub.cyanlabphotogallery.view.GalleryAdapter
import comonotolo.httpsgithub.cyanlabphotogallery.view.ImageHolder


/**
 *  Abstract class that defines common methods shared by all Fragments that must represent grid of images
 */
abstract class GalleryFragment : Fragment() {

    companion object {
        val MODE_RECENT = R.string.mode_recent
        val MODE_TOP = R.string.mode_top
        val MODE_FAVORITES = R.string.mode_favorites
    }

    /**
     * Three lists which contain info about images
     */
    var imagesNames = ArrayList<String?>()
    var imagesHrefs = ArrayList<String?>()
    var likeFlags = ArrayList<Boolean>()

    abstract val mode: Int

    /**
     * Link to the next page of photos
     */
    var nextHref: String? = null

    var recycler: RecyclerView? = null

    var spanCount = 2

    private val EXTRA_NAMES = "names $mode"
    private val EXTRA_HREFS = "hrefs $mode"
    private val EXTRA_LIKES = "likes $mode"
    val EXTRA_SCROLL_POSITION = "scroll $mode"

    /**
     *  Returns default url link depending on mode of a Fragment
     */
    fun defaultURL() =
            "http://api-fotki.yandex.ru/api/${resources.getString(mode)}/updated/"

    var isLoading = false

    /**
     * Flag that is true when there is no href to the next page of images
     */
    var isBottomReached = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_images, container, false)

        recycler = view?.findViewById(R.id.recycler_view)

        if (savedInstanceState == null) {

            loadImages()
            return view
        }

        // Restoring state

        imagesNames = savedInstanceState.getStringArrayList(EXTRA_NAMES)
        imagesHrefs = savedInstanceState.getStringArrayList(EXTRA_HREFS)
        val likes = savedInstanceState.getBooleanArray(EXTRA_LIKES)

        likeFlags = ArrayList()
        for (like in likes)
            likeFlags.add(like)

        isLoading = false

        (activity as MainActivity).invalidateFab(this)

        return view

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        prepareRecycler()

    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putStringArrayList(EXTRA_NAMES, imagesNames)
        outState.putStringArrayList(EXTRA_HREFS, imagesHrefs)
        outState.putBooleanArray(EXTRA_LIKES, likeFlags.toBooleanArray())

        super.onSaveInstanceState(outState)
    }

    open fun prepareRecycler() {

        val size = Point()

        activity?.windowManager?.defaultDisplay?.getSize(size)

        val width = size.x

        spanCount = width / 400 + if (width % 400 != 0) 1 else 0

        recycler?.layoutManager = GridLayoutManager(context, spanCount)

        recycler?.adapter = GalleryAdapter(imagesHrefs, imagesNames, spanCount, this)

        recycler?.addOnChildAttachStateChangeListener((activity as MainActivity).FabInvalidator(this))

        recycler?.setRecyclerListener {

            val imageHolder = it as ImageHolder

            Picasso.get().cancelRequest(imageHolder.image)
        }

    }

    abstract fun loadImages(url: String? = null)

}