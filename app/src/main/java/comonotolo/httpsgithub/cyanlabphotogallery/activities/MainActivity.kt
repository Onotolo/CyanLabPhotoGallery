package comonotolo.httpsgithub.cyanlabphotogallery.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.net.ConnectivityManager
import android.net.Network
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
import java.io.File
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener {

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        if (isLoading)
            tab?.select()
    }

    companion object {

        val images = ArrayList<Bitmap?>()
        val imagesHrefs = ArrayList<String?>()
        val likeFlags = ArrayList<Boolean>()

        val MODE_RECENT = R.string.mode_recent
        val MODE_TOP = R.string.mode_top
        val MODE_FAVORITES = R.string.mode_favorites

        val INTENT_EXTRA_IMAGE_HREF = "Image href"

        var imagePosition = 0

        var mode = MainActivity.MODE_RECENT

        val REQUEST_CODE_SHOW = 1
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {

        if (!isLoading) {
            val position = tab?.position

            mode = when (position){
                0 -> MODE_RECENT
                1 -> MODE_TOP
                2 -> MODE_FAVORITES
                else -> MODE_RECENT
            }

            images.removeAll(images)
            imagesHrefs.removeAll(imagesHrefs)
            likeFlags.removeAll(likeFlags)

            when (position){
                2 -> loadFavorites()
                else -> loadImagesFromNet()
            }
        }
    }

    var nextHref: String? = null

    var recycler: RecyclerView? = null

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

        loadImagesFromNet()

    }

    fun loadFavorites(){

        recycler?.scheduleLayoutAnimation()

        isLoading = true

        val favoritesHrefs = filesDir.list()

        var count = 0

        val load = thread {

            val size = Point()
            windowManager.defaultDisplay.getSize(size)

            val width = size.x / 2

            for (favorite in favoritesHrefs){

                if (favorite.endsWith("@small.png")) {

                    imagesHrefs.add(favorite.removeSuffix("@small.png"))

                    images.add(Picasso.get()
                            .load(File("${filesDir.absolutePath}/$favorite"))
                            .resize(width, width)
                            .centerCrop()
                            .get())

                    count++
                }
            }
        }

        load.join()

        while (images.size != count){}

        recycler?.adapter?.notifyDataSetChanged()


    }

    fun loadImagesFromNet(url: String? = defaultURL()){

        val netInfo = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (netInfo != null && netInfo.isConnectedOrConnecting) {

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

                fillLikeFlags(newImagesHrefs)

                while (images.size != imagesHrefs.size){}

                runOnUiThread {

                    isLoading = false

                    if (imagesHrefs.size < 21)
                        recycler?.scheduleLayoutAnimation()

                    recycler?.adapter?.notifyDataSetChanged()

                }
            }
        }
    }

    fun fillLikeFlags(newHrefs: ArrayList<String>){

        val favorites = filesDir.list()

        for (href in newHrefs){

            likeFlags.add(favorites.contains("${href.replace('/', '@')}.png"))

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode){

            REQUEST_CODE_SHOW -> {
                if (resultCode == Activity.RESULT_OK){
                    if (mode != MODE_FAVORITES) likeFlags[imagePosition] = data?.getBooleanExtra(ImageActivity.INTENT_EXTRA_IS_FAVORITE, false) == true
                    recycler?.adapter?.notifyItemChanged(imagePosition)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
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
                    loadImagesFromNet(nextHref)

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
