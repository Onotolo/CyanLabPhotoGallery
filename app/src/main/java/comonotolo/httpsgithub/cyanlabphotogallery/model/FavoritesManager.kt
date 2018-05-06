package comonotolo.httpsgithub.cyanlabphotogallery.model

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import java.io.BufferedOutputStream
import kotlin.concurrent.thread


/**
 *  This class is responsible for saving and deleting images from internal storage
 */
class FavoritesManager(val activity: Activity?) {

    var isInProcess = false

    /**
     * Asynchronously saves to or deletes image from internal store.
     * When process is complete, it notifies all listeners
     */
    fun handleLikeEvent(imageName: String?, isLiked: Boolean, bitmap: Bitmap?): Boolean {

        if (activity == null)
            return false

        val favorites = activity.filesDir?.list()

        val name = imageName?.replace('.', '@')

        if (!isLiked && (favorites?.contains("$name.png") == true)) {

            isInProcess = true

            thread {
                activity.deleteFile("$name.png")

                isInProcess = false
            }

            notifyListeners(imageName, isLiked)

            return true
        }

        if (isLiked && (favorites?.contains("$name.png") != true) && bitmap != null) {

            isInProcess = true

            thread {

                val out = BufferedOutputStream(activity.openFileOutput("$name.png", Context.MODE_PRIVATE))

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

                isInProcess = false

                notifyListeners(imageName, isLiked)

            }
            return true
        }

        return false
    }

    /**
     * Interface for classes which need notifications about image saving process
     */
    interface OnFavoriteChangedListener {

        fun onFavoriteChanged(imageName: String?, isLiked: Boolean)
    }

    /**
     * Static list of all listeners
     */
    companion object {

        private val listeners = ArrayList<OnFavoriteChangedListener?>()
    }


    fun addOnFavoriteChangedListener(onFavoriteChangedListener: OnFavoriteChangedListener): Boolean {

        if (listeners.contains(onFavoriteChangedListener))
            return false

        listeners.add(onFavoriteChangedListener)
        return true
    }

    fun removeOnFavoriteChangedListener(onFavoriteChangedListener: OnFavoriteChangedListener): Boolean {

        if (!listeners.contains(onFavoriteChangedListener))
            return false

        listeners.remove(onFavoriteChangedListener)
        return true
    }

    /**
     *  Method for notifying all listeners
     */
    fun notifyListeners(imageName: String?, isLiked: Boolean) {
        for (listener in listeners) {
            listener?.onFavoriteChanged(imageName, isLiked)
        }
    }
}