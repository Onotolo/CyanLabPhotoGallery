package comonotolo.httpsgithub.cyanlabphotogallery.view

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import comonotolo.httpsgithub.cyanlabphotogallery.activities.ImageActivity
import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity

class ImageHolder(val activity: MainActivity, imageView: View): RecyclerView.ViewHolder(imageView), View.OnClickListener{

    init {
        imageView.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {

        val href = activity.imagesHrefs[adapterPosition]

        val data = Intent(activity, ImageActivity::class.java)

        data.putExtra(activity.INTENT_IMAGE_HREF, href)

        activity.startActivity(data)
    }
}