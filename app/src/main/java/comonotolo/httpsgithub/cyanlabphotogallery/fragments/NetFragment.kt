package comonotolo.httpsgithub.cyanlabphotogallery.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity
import comonotolo.httpsgithub.cyanlabphotogallery.network.URLResponseParser
import comonotolo.httpsgithub.cyanlabphotogallery.view.ImageHolder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import kotlin.concurrent.thread

/**
 * Fragment that loads pictures from Net
 */
abstract class NetFragment : GalleryFragment(), MainActivity.OnLikeListener {

    private val client = OkHttpClient()


    /**
     * Huge method for loading images from Яндекс Фотки{https://fotki.yandex.ru/}
     */
    override fun loadImages(url: String?) {

        val netInfo = (activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?)?.activeNetworkInfo

        if (netInfo == null || !netInfo.isConnectedOrConnecting) {

            recycler?.visibility = View.GONE

            if (refresh_layout?.visibility != View.GONE)
                activity?.refresh_layout?.visibility = View.GONE

            return
        }

        isLoading = true



        if (refresh_layout?.visibility != View.VISIBLE) {

            refresh_layout?.visibility = View.VISIBLE

            refresh_layout?.alpha = 0f

            refresh_layout?.animate()?.alpha(1f)?.setDuration(225)?.start()
        }

        if (url.equals(defaultURL())) {
            isBottomReached = false
        }

        thread(isDaemon = true) {

            val request = Request.Builder()
                    .get()
                    .addHeader("Host", "api-fotki.yandex.ru")
                    .addHeader("Connection", "keep-alive")
                    .url(url ?: defaultURL())
                    .tag("HTTP/1.1")
                    .build()

            val response = client.newCall(request)?.execute()

            if (response?.isSuccessful != true) {

                isLoading = false

                activity?.runOnUiThread {
                    if (refresh_layout?.visibility != View.GONE)
                        activity?.refresh_layout?.visibility = View.GONE
                }

                return@thread
            }

            val parsedResponse = try {

                URLResponseParser().parseResponse(response.body()?.string())

            } catch (ex: IOException) {
                isLoading = false

                activity?.runOnUiThread {
                    if (refresh_layout?.visibility != View.GONE)
                        activity?.refresh_layout?.visibility = View.GONE
                }

                return@thread
            }

            val newImagesHrefs = parsedResponse.imagesHrefs

            var newImagesShift = 0

            parsedResponse.imagesHrefs.forEach {
                if (!imagesHrefs.contains(it)) {
                    imagesHrefs.add(newImagesShift, it)
                    imagesNames.add(newImagesShift++, parsedResponse.imagesNames[newImagesHrefs.indexOf(it)])
                }
            }

            isBottomReached = parsedResponse.isBottomReached

            nextHref = parsedResponse.nextHref

            fillLikeFlags()

            activity?.runOnUiThread {

                isLoading = false

                val oldPosition = imagesHrefs.size

                recycler?.visibility =
                        if (imagesNames.size == 0) View.GONE
                        else View.VISIBLE


                recycler?.adapter?.notifyItemRangeInserted(oldPosition, newImagesHrefs.size)

                if (refresh_layout?.visibility != View.GONE)
                    activity?.refresh_layout?.visibility = View.GONE


            }
        }
    }


    /**
     * Like flag shows whether the image is in favorites or not
     */
    private fun fillLikeFlags() {

        val favorites = activity?.filesDir?.list()

        for (i in imagesNames.indices) {

            val name = imagesNames[i]

            val isFavorite = favorites?.contains("${name?.replace('.', '@')}.png") == true

            if (i < likeFlags.size)

                likeFlags[i] = isFavorite
            else
                likeFlags.add(isFavorite)
        }


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as MainActivity).addOnLikeListener(this)
    }


    /**
     * Puts like-mark on each liked and removes one from disliked image
     */
    override fun onLikeEvent(imageName: String?, isLiked: Boolean) {

        val index = imagesNames.indexOf(imageName)

        if (index != -1) {

            likeFlags[index] = isLiked

            val holder = recycler?.findViewHolderForAdapterPosition(index)

            (holder as ImageHolder?)?.like?.visibility = if (isLiked) View.VISIBLE else View.GONE
        }
    }


}