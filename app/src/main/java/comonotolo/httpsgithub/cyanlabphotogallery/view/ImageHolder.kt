package comonotolo.httpsgithub.cyanlabphotogallery.view

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.support.v7.widget.RecyclerView
import android.view.View
import comonotolo.httpsgithub.cyanlabphotogallery.activities.ImageActivity
import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity
import kotlinx.android.synthetic.main.image_holder.view.*

class ImageHolder(val activity: MainActivity, imageView: View): RecyclerView.ViewHolder(imageView), View.OnClickListener{

    val image = imageView.holder_image
    val like = imageView.holder_like

    init {
        imageView.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {

        val href = MainActivity.Companion.imagesHrefs[adapterPosition]

        val data = Intent(activity, ImageActivity::class.java)

        data.putExtra(MainActivity.INTENT_EXTRA_IMAGE_HREF, href)

        MainActivity.imagePosition = adapterPosition

        val imageBMP = (image.drawable as BitmapDrawable).bitmap

        MainActivity.bitmapAtPosition = imageBMP

        activity.startActivityForResult(data, MainActivity.REQUEST_CODE_SHOW)
    }
}