package comonotolo.httpsgithub.cyanlabphotogallery.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.R
import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity
import java.io.File

class GalleryAdapter(private val imagesHrefs: List<String?>, val  activity: MainActivity): RecyclerView.Adapter<ImageHolder>() {

    var width = -1

    init {
        val size = Point()
        activity.windowManager.defaultDisplay.getSize(size)
        width = size.x / 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {

        val imageView = LayoutInflater.from(parent.context).inflate(R.layout.image_holder, parent, false)

        imageView.layoutParams.height = (width * 3)/4

        return ImageHolder(activity, imageView)
    }

    override fun getItemCount(): Int {
        return imagesHrefs.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {

        val favorites = activity.filesDir.list()

        when(MainActivity.mode){
            MainActivity.MODE_FAVORITES -> {
                Picasso.get().load(File("${activity.filesDir.absolutePath}/${imagesHrefs[position]}@small.png")).into(holder.image)
            }
            else -> {
                if (!favorites.contains("${imagesHrefs[position]?.replace('/', '@')}.png")) {
                    Picasso.get().load("${imagesHrefs[position]}_L").centerCrop().resize(width, (width * 3)/4).memoryPolicy(MemoryPolicy.NO_STORE).into(holder.image)
                }else
                    Picasso.get().load(File("${activity.filesDir.absolutePath}/${imagesHrefs[position]?.replace('/','@')}@small.png")).into(holder.image)
            }
        }

        if (MainActivity.mode != MainActivity.MODE_FAVORITES)
            holder.like.visibility = if (MainActivity.likeFlags[position]) View.VISIBLE else View.GONE
        else
            holder.like.visibility = View.GONE

    }
}