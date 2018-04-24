package comonotolo.httpsgithub.cyanlabphotogallery.view

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import comonotolo.httpsgithub.cyanlabphotogallery.R
import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity

class GalleryAdapter(private val images: List<Bitmap?>,val  activity: MainActivity): RecyclerView.Adapter<ImageHolder>() {

    val height = activity.resources.displayMetrics.density;

    var width = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {

        val imageView = LayoutInflater.from(parent.context).inflate(R.layout.image_holder, parent, false)

        //imageView.layoutParams.height = parent.width/2

        imageView.setOnClickListener {

        }

        if (width == -1) width = ((parent.width/3 - 4 * height).toInt())

        return ImageHolder(activity, imageView)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {

        holder.image.setImageBitmap(images[position])
        if (MainActivity.mode != MainActivity.MODE_FAVORITES) holder.like.visibility = if (MainActivity.likeFlags[position]) View.VISIBLE else View.GONE

    }
}