package comonotolo.httpsgithub.cyanlabphotogallery.activities

import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.squareup.picasso.Picasso
import comonotolo.httpsgithub.cyanlabphotogallery.R
import comonotolo.httpsgithub.cyanlabphotogallery.network.URLResponseParser
import comonotolo.httpsgithub.cyanlabphotogallery.view.GalleryAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener {

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {

        val position = tab?.position

        mode = when (position){
            0 -> MODE_RECENT
            1 -> MODE_TOP
            2 -> MODE_FAVORITES
            else -> MODE_RECENT
        }

        images.removeAll(images)
        imagesHrefs.removeAll(imagesHrefs)

        loadImages()
    }

    val INTENT_IMAGE_HREF = "Image href"

    var nextHref: String? = null

    var recycler: RecyclerView? = null

    val images = ArrayList<Bitmap?>()
    val imagesHrefs = ArrayList<String?>()

    val MODE_RECENT = R.string.mode_recent
    val MODE_TOP = R.string.mode_top
    val MODE_POD = R.string.mode_pod
    val MODE_FAVORITES = R.string.mode_favorites

    var mode = MODE_RECENT

    var limit = 21

    var spanCount = 2

    fun defaultURL() =
            "http://api-fotki.yandex.ru/api/${resources.getString(mode)}/?limit=$limit"

    var isLoading = false
    var isBottomReached = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)

        tabLayout.addOnTabSelectedListener(this)

        prepareRecycler()

        recycler?.scheduleLayoutAnimation()

        loadImages()

    }

    fun loadImages(url: String? = defaultURL()){

        if (recycler?.layoutManager?.itemCount != imagesHrefs.size)
            return

        isLoading = true


        thread (true){

            if (url.equals(defaultURL())){

                isBottomReached = false
                recycler?.scheduleLayoutAnimation()

            }

            val request = Request.Builder()
                    .get()
                    .addHeader("Host", "api-fotki.yandex.ru")
                    .addHeader("Connection", "keep-alive")
                    .url(url ?: defaultURL())
                    .tag("HTTP/1.1")
                    .build()

            val client = OkHttpClient()

            val response = client.newCall(request).execute()

            if (response.code() != 200) {
                isLoading = false
                return@thread
            }

            val parsedResponse = URLResponseParser().parseResponse(response.body()?.string())

            val newImagesHrefs = parsedResponse.imagesHrefs

            isBottomReached = parsedResponse.isBottomReached

            nextHref = parsedResponse.nextHref

            imagesHrefs.addAll(newImagesHrefs)

            if (newImagesHrefs.size > 0){

                val size = Point()
                windowManager.defaultDisplay.getSize(size)

                val width = size.x / 2

                for (nameIndex in newImagesHrefs.indices){
                    images.add(Picasso.get()
                            .load(newImagesHrefs[nameIndex] + "_L")
                            .resize(width, width)
                            .centerCrop()
                            .get())
                }

            }else{

                isBottomReached = true

            }

            while (images.size != imagesHrefs.size){}

            runOnUiThread {

                isLoading = false

                if (imagesHrefs.size < 21)
                    recycler?.scheduleLayoutAnimation()

                recycler?.adapter?.notifyDataSetChanged()

            }


        }

    }


    fun prepareRecycler(){

        recycler = findViewById(R.id.recycler_view)

        recycler?.layoutManager = GridLayoutManager(this, 2)

        recycler?.adapter = GalleryAdapter(images, this)

        recycler?.setRecyclerListener{

            val manager = recycler?.layoutManager as GridLayoutManager

            if (!isLoading && !isBottomReached){

                val ic = manager.itemCount

                val flv = manager.findLastVisibleItemPosition()

                if (ic  - flv < 7 && ic == imagesHrefs.size){

                    val nextHref = nextHref
                    loadImages(nextHref)

                }
            }

        }

        //recycler?.addOnChildAttachStateChangeListener(OnChildStateChangedLoader(recycler?.layoutManager as GridLayoutManager, this))
    }

    class OnChildStateChangedLoader(val manager: GridLayoutManager, val activity: MainActivity): RecyclerView.OnChildAttachStateChangeListener{

        override fun onChildViewDetachedFromWindow(view: View?) {



        }

        override fun onChildViewAttachedToWindow(view: View?) {
        }

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}
