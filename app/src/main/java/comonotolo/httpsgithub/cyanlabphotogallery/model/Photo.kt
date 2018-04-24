package comonotolo.httpsgithub.cyanlabphotogallery.model

import android.graphics.Bitmap
import java.io.Serializable

class Photo(val href: String, val id: String, val image: Bitmap): Serializable {

    fun getOriginalHref(): String = "${href}_orig"

    fun getCroppedHref(postfix: String): String = href + postfix
}