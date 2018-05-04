package comonotolo.httpsgithub.cyanlabphotogallery.model

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import java.io.BufferedOutputStream
import kotlin.concurrent.thread

class FavoritesManager(val activity: Activity?) {

    fun handleLikeEvent(imageName: String?, isLiked: Boolean, bitmap: Bitmap?): Boolean {

        if (activity == null)
            return false

        val favorites = activity.filesDir?.list()

        val name = imageName?.replace('.', '@')

        if (!isLiked && (favorites?.contains("$name.png") == true)) {

            thread {
                activity.deleteFile("$name.png")
            }
            return true
        }

        if (isLiked && (favorites?.contains("$name.png") != true) && bitmap != null) {

            thread {

                val out = BufferedOutputStream(activity.openFileOutput("$name.png", Context.MODE_PRIVATE))

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

            }
            return true
        }

        return false
    }
}