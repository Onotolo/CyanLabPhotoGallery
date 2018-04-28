package comonotolo.httpsgithub.cyanlabphotogallery.view

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import comonotolo.httpsgithub.cyanlabphotogallery.activities.ImageActivity
import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.FavoriteFragment
import comonotolo.httpsgithub.cyanlabphotogallery.fragments.ImagesFragment
import kotlinx.android.synthetic.main.image_holder.view.*

class ImageHolder(val fragment: ImagesFragment, imageView: View) : RecyclerView.ViewHolder(imageView), View.OnClickListener {

    val image = imageView.holder_image
    val like = imageView.holder_like

    init {
        imageView.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {

        val href = when (fragment) {
            is FavoriteFragment -> fragment.imagesNames[adapterPosition]
            else -> fragment.imagesHrefs[adapterPosition]
        }

        val data = Intent(fragment.activity, ImageActivity::class.java)

        data.putExtra(MainActivity.INTENT_EXTRA_POSITION, adapterPosition)
                .putExtra(MainActivity.INTENT_EXTRA_IMAGE_NAME, fragment.imagesNames[adapterPosition])
                .putExtra(MainActivity.INTENT_EXTRA_IMAGE_HREF, href)

        fragment.activity?.startActivityForResult(data, MainActivity.REQUEST_CODE_SHOW)
    }
}