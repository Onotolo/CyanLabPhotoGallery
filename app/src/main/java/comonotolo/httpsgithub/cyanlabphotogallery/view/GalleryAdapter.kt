package comonotolo.httpsgithub.cyanlabphotogallery.view

import android.graphics.Point
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.R
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.ImagesFragment
import java.io.File

class GalleryAdapter(private val imagesHrefs: List<String?>, private val imagesNames: ArrayList<String?>, val spanCount: Int, private val fragment: ImagesFragment) : RecyclerView.Adapter<ImageHolder>() {

    var width = -1

    private val dirPath = fragment.activity?.filesDir?.absolutePath

    init {
        val size = Point()

        fragment.activity?.windowManager?.defaultDisplay?.getSize(size)

        width = size.x

    }

    var height = width / spanCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {

        val imageView = LayoutInflater.from(parent.context).inflate(R.layout.image_holder, parent, false)

        imageView.layoutParams.height = width / spanCount

        return ImageHolder(fragment, imageView)
    }

    override fun getItemCount(): Int {

        return when (fragment.mode) {

            ImagesFragment.MODE_FAVORITES -> imagesNames.size

            else -> imagesHrefs.size
        }
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {

        val favorites = fragment.activity?.filesDir?.list()

        if (!(fragment.mode == ImagesFragment.MODE_FAVORITES || favorites?.contains("${imagesNames[position]?.replace('.', '@')}.png") == true)) {

            Picasso.get()
                    .load("${imagesHrefs[position]}_L")
                    .centerCrop()
                    .resize(height, height)
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .into(holder.image)
        } else
            Picasso.get()
                    .load(File("$dirPath/${imagesNames[position]?.replace('.', '@')}.png"))
                    .memoryPolicy(MemoryPolicy.NO_STORE, MemoryPolicy.NO_CACHE)
                    .resize(height, height)
                    .centerCrop()
                    .into(holder.image)


        if (fragment.mode != ImagesFragment.MODE_FAVORITES)

            holder.like.visibility = if (fragment.likeFlags[position]) View.VISIBLE else View.GONE
        else
            holder.like.visibility = View.GONE

    }
}