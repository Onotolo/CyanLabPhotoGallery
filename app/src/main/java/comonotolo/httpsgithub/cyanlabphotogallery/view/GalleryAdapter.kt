package comonotolo.httpsgithub.cyanlabphotogallery.view

import android.graphics.Point
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.R
import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.ImagesFragment
import java.io.File

class GalleryAdapter(private val imagesHrefs: List<String?>, val fragment: ImagesFragment) : RecyclerView.Adapter<ImageHolder>() {

    var width = -1

    init {
        val size = Point()
        fragment.activity?.windowManager?.defaultDisplay?.getSize(size)
        width = size.x / 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {

        val imageView = LayoutInflater.from(parent.context).inflate(R.layout.image_holder, parent, false)

        imageView.layoutParams.height = (width * 3)/4

        return ImageHolder(fragment, imageView)
    }

    override fun getItemCount(): Int {
        return when (fragment.mode) {
            MainActivity.MODE_FAVORITES -> fragment.imagesNames.size
            else -> imagesHrefs.size
        }
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {

        val favorites = fragment.activity?.filesDir?.list()

        when (fragment.mode) {
            MainActivity.MODE_FAVORITES -> {
                Picasso.get().load(File("${fragment.activity?.filesDir?.absolutePath}/${fragment.imagesNames[position]}_small.png")).into(holder.image)
            }
            else -> {
                if (!(favorites?.contains("${fragment.imagesNames[position]}.png") == true)) {
                    Picasso.get().load("${imagesHrefs[position]}_M").centerCrop().resize(width, (width * 3) / 4).memoryPolicy(MemoryPolicy.NO_STORE).into(holder.image)
                }else
                    Picasso.get().load(File("${fragment.activity?.filesDir?.absolutePath}/${fragment.imagesNames[position]}_small.png")).into(holder.image)
            }
        }

        if (fragment.mode != MainActivity.MODE_FAVORITES)
            holder.like.visibility = if (fragment.likeFlags[position]) View.VISIBLE else View.GONE
        else
            holder.like.visibility = View.GONE

    }
}