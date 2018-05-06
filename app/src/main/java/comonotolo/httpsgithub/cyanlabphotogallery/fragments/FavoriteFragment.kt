package comonotolo.httpsgithub.cyanlabphotogallery.fragments

import android.view.View
import comonotolo.httpsgithub.cyanlabphotogallery.model.FavoritesManager
import kotlinx.android.synthetic.main.fragment_images.*
import kotlin.concurrent.thread

/**
 * Gallery Fragment inerrant that shows Favorite pictures.
 *
 * Favorite pictures are stored in internal storage and user is not given a direct access to them.
 * To learn more about favorites storage @see FavoritesManager
 */
class FavoriteFragment : GalleryFragment(), FavoritesManager.OnFavoriteChangedListener {


    /**
     * This method is called when FavoritesManager finishes saving or removing file with image.
     */
    override fun onFavoriteChanged(imageName: String?, isLiked: Boolean) {

        activity?.runOnUiThread {
            if (isLiked && !imagesNames.contains(imageName)) {

                imagesNames.add(imageName)
                recycler?.adapter?.notifyItemInserted(imagesNames.size)

                //recycler?.adapter?.notifyDataSetChanged()

            } else if (!isLiked && imagesNames.contains(imageName)) {

                val index = imagesNames.indexOf(imageName)

                imagesNames.remove(imageName)
                recycler?.adapter?.notifyItemRemoved(index)
            }

            if (imagesNames.size == 0) {
                recycler?.visibility = View.GONE
                fav_info.visibility = View.VISIBLE
            } else
                recycler?.visibility = View.VISIBLE
        }
    }

    override val mode = MODE_FAVORITES


    override fun loadImages(url: String?) {

        if (isLoading)
            return

        thread(isDaemon = true) {

            isLoading = true

            val favorites = activity?.filesDir?.list()

            if (favorites == null) {

                isLoading = false
                return@thread
            }

            for (favorite in favorites) {

                val name = favorite.removeSuffix(".png").replace('@', '.')

                if (!imagesNames.contains(name)) {

                    imagesNames.add(name)
                }
            }

            activity?.runOnUiThread {

                recycler?.adapter?.notifyDataSetChanged()

                isLoading = false

                if (imagesNames.size == 0) {
                    recycler?.visibility = View.GONE
                    fav_info.visibility = View.VISIBLE
                } else
                    recycler?.visibility = View.VISIBLE
            }
        }
    }

    override fun onStart() {

        FavoritesManager(activity).addOnFavoriteChangedListener(this)

        super.onStart()
    }
}