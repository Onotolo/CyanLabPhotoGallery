package comonotolo.httpsgithub.cyanlabphotogallery.fragments

import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import comonotolo.httpsgithub.cyanlabphotogallery.R
import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity
import comonotolo.httpsgithub.cyanlabphotogallery.view.GalleryAdapter

abstract class GalleryFragment() : Fragment() {

    companion object {
        val MODE_RECENT = R.string.mode_recent
        val MODE_TOP = R.string.mode_top
        val MODE_FAVORITES = R.string.mode_favorites
    }

    val imagesNames = ArrayList<String?>()

    val imagesHrefs = ArrayList<String?>()

    val likeFlags = ArrayList<Boolean>()

    abstract val mode: Int

    var nextHref: String? = null

    var recycler: RecyclerView? = null

    var spanCount = 2

    fun defaultURL() =
            "http://api-fotki.yandex.ru/api/${resources.getString(mode)}/"

    var isLoading = false
    var isBottomReached = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_images, container, false)

        recycler = view?.findViewById(R.id.recycler_view)

        recycler?.scheduleLayoutAnimation()

        prepareRecycler()

        loadImages()

        return view
    }


    open fun prepareRecycler() {

        val size = Point()

        activity?.windowManager?.defaultDisplay?.getSize(size)

        val width = size.x

        spanCount = width / 400 + if (width % 400 != 0) 1 else 0

        recycler?.layoutManager = GridLayoutManager(context, spanCount)

        recycler?.adapter = GalleryAdapter(imagesHrefs, imagesNames, spanCount, this)

        recycler?.addOnChildAttachStateChangeListener((activity as MainActivity).OnChildStateChangedLoader(recycler?.layoutManager as GridLayoutManager))

        recycler?.onChildAttachedToWindow(null)
    }

    abstract fun loadImages(url: String? = null)

    abstract fun onLikeEvent(imageName: String?, isLiked: Boolean)


}