package comonotolo.httpsgithub.cyanlabphotogallery.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.view.View
import comonotolo.httpsgithub.cyanlabphotogallery.network.URLResponseParser
import comonotolo.httpsgithub.cyanlabphotogallery.view.ImageHolder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import kotlin.concurrent.thread

abstract class NetFragment() : ImagesFragment() {

    val client = OkHttpClient()

    override fun loadImages(url: String?) {

        val netInfo = (activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (netInfo != null && netInfo.isConnectedOrConnecting) {

            isLoading = true

            val refreshLayout = activity?.refresh_layout

            refreshLayout?.visibility = View.VISIBLE

            refreshLayout?.alpha = 0f

            refreshLayout?.animate()?.alpha(1f)?.setDuration(225)?.start()

            thread(true) {

                if (url.equals(defaultURL())) {
                    isBottomReached = false
                }

                val request = Request.Builder()
                        .get()
                        .addHeader("Host", "api-fotki.yandex.ru")
                        //.addHeader("Connection", "keep-alive")
                        .url(url ?: defaultURL())
                        .tag("HTTP/1.1")
                        .build()

                val response = client.newCall(request)?.execute()

                if (!(response?.isSuccessful == true)) {

                    isLoading = false

                    return@thread
                }

                val parsedResponse = try {

                    URLResponseParser().parseResponse(response.body()?.string())

                } catch (ex: IOException) {
                    isLoading = false

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

                    recycler?.adapter?.notifyItemRangeInserted(oldPosition, newImagesHrefs.size)

                    activity?.refresh_layout?.visibility = View.GONE

                }
            }
        }
    }

    fun fillLikeFlags() {

        val favorites = activity?.filesDir?.list()

        for (name in imagesNames) {

            val isFavorite = favorites?.contains("${name?.replace('.', '@')}.png") == true

            if (imagesNames.indexOf(name) < likeFlags.size)
                likeFlags[imagesNames.indexOf(name)] = isFavorite
            else
                likeFlags.add(isFavorite)
        }
    }

    override fun onLikeEvent(imageName: String?, isLiked: Boolean) {

        val index = imagesNames.indexOf(imageName)

        if (index != -1) {

            likeFlags[index] = isLiked

            val holder = recycler?.findViewHolderForAdapterPosition(index)

            (holder as ImageHolder?)?.like?.visibility = if (isLiked) View.VISIBLE else View.GONE
        }
    }


}