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
import java.io.IOException
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener {

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    companion object {

        val imagesNames = ArrayList<String?>()
        val imagesHrefs = ArrayList<String?>()
        val likeFlags = ArrayList<Boolean>()

        val MODE_RECENT = R.string.mode_recent
        val MODE_TOP = R.string.mode_top
        val MODE_FAVORITES = R.string.mode_favorites

        val INTENT_EXTRA_IMAGE_HREF = "Image href"

        var imagePosition = 0
        var bitmapAtPosition: Bitmap? = null

        var mode = MainActivity.MODE_RECENT

        val REQUEST_CODE_SHOW = 1
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {

        val position = tab?.position

        mode = when (position){
            0 -> MODE_RECENT
            1 -> MODE_TOP
            2 -> MODE_FAVORITES
            else -> MODE_RECENT
        }

        imagesNames.removeAll(imagesNames)
        imagesHrefs.removeAll(imagesHrefs)
        likeFlags.removeAll(likeFlags)
        limit = 20

        nextHref = null

        recycler?.adapter?.notifyDataSetChanged()

        when (position){
            2 -> loadFavorites()
            else -> loadImagesFromNet()
        }

    }

    var nextHref: String? = null

    var recycler: RecyclerView? = null

    var limit = 20

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

        for (favorite in favoritesHrefs){

            if (favorite.endsWith("@small.png")) {

                imagesHrefs.add(favorite.removeSuffix("@small.png"))

                count++
            }
        }

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

                if (!response.isSuccessful) {

                    isLoading = false

                    return@thread
                }

                val parsedResponse = try {

                    URLResponseParser().parseResponse(response.body()?.string())

                }catch (ex: IOException){
                    isLoading = false

                    return@thread
                }

                val newImagesHrefs = parsedResponse.imagesHrefs

                imagesNames.addAll(parsedResponse.imagesNames)

                isBottomReached = parsedResponse.isBottomReached

                nextHref = parsedResponse.nextHref

                fillLikeFlags(newImagesHrefs)

                runOnUiThread {

                    isLoading = false

                    val oldPosition = imagesHrefs.size

                    imagesHrefs.addAll(newImagesHrefs)

                    if (!isBottomReached && limit == 20)
                        limit = 6

                    recycler?.adapter?.notifyItemRangeInserted(oldPosition, newImagesHrefs.size)

                }
            }
        }
    }

    fun fillLikeFlags(newHrefs: ArrayList<String?>){

        val favorites = filesDir.list()

        for (href in newHrefs){

            likeFlags.add(favorites.contains("${href?.replace('/', '@')}.png"))

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when (requestCode){

            REQUEST_CODE_SHOW -> {
                if (resultCode == Activity.RESULT_OK){
                    if (mode != MODE_FAVORITES){

                        likeFlags[imagePosition] = data?.getBooleanExtra(ImageActivity.INTENT_EXTRA_IS_FAVORITE, false) == true

                        recycler?.adapter?.notifyItemChanged(imagePosition)

                    }
                    else if (data?.getBooleanExtra(ImageActivity.INTENT_EXTRA_IS_FAVORITE, false) != true){

                        imagesHrefs.removeAt(imagePosition)

                        recycler?.adapter?.notifyItemRemoved(imagePosition)
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    fun prepareRecycler(){

        recycler = findViewById(R.id.recycler_view)

        recycler?.layoutManager = GridLayoutManager(this, 2)

        recycler?.adapter = GalleryAdapter(imagesHrefs, this)

        recycler?.addOnChildAttachStateChangeListener(OnChildStateChangedLoader(recycler?.layoutManager as GridLayoutManager, this))
    }

    class OnChildStateChangedLoader(val manager: GridLayoutManager, val activity: MainActivity): RecyclerView.OnChildAttachStateChangeListener{

        override fun onChildViewDetachedFromWindow(view: View?) {

            if (!activity.isLoading && !activity.isBottomReached){

                val ic = manager.itemCount

                val flv = manager.findLastVisibleItemPosition()

                if (ic  - flv < 7){

                    val nextHref = activity.nextHref
                    activity.loadImagesFromNet(nextHref?.replace("limit=20", "limit=${activity.limit}"))

                }
            }

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
