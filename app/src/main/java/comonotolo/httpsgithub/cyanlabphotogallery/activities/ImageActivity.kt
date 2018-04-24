package comonotolo.httpsgithub.cyanlabphotogallery.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.R

class ImageActivity : AppCompatActivity() {

    val INTENT_IMAGE_HREF = "Image href"

    var imageHref: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        imageHref = intent.getStringExtra(INTENT_IMAGE_HREF)

        val imageView = findViewById<ImageView>(R.id.image_place)

        Picasso.get().load("${imageHref}_XXL").into(imageView)

    }
}