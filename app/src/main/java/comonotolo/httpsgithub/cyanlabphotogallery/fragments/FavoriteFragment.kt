package comonotolo.httpsgithub.cyanlabphotogallery.fragments

import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity

class FavoriteFragment : ImagesFragment() {


    override fun onLikeEvent(imageName: String?, isLiked: Boolean) {

        if (isLiked && !imagesNames.contains(imageName)) {

            imagesNames.add(imageName)
            recycler?.adapter?.notifyItemInserted(imagesNames.lastIndex)

        } else if (!isLiked && imagesNames.contains(imageName)) {

            val index = imagesNames.indexOf(imageName)

            imagesNames.remove(imageName)
            recycler?.adapter?.notifyItemRemoved(index)
        }
    }

    override val mode = MainActivity.MODE_FAVORITES

    override fun loadImages(url: String?) {

        val favorites = activity?.filesDir?.list()

        val oldImagesCount = imagesNames.size

        var count = 0

        if (favorites != null) {
            for (favorite in favorites) {

                val name = favorite.removeSuffix(".png").replace('@', '.')

                if (!imagesNames.contains(name)) {

                    imagesNames.add(name)
                    count++
                }
            }
        }

        recycler?.adapter?.notifyItemRangeInserted(oldImagesCount, count)
    }

    override fun onStart() {

        loadImages()

        super.onStart()
    }
}