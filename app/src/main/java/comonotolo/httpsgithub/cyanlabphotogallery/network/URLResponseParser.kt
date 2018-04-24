package comonotolo.httpsgithub.cyanlabphotogallery.network

import comonotolo.httpsgithub.cyanlabphotogallery.activities.MainActivity

class URLResponseParser {

    private val TAG_ENTRY = "<entry>"
    private val TAG_CLOSE_ENTRY = "</entry>"
    private val TAG_CONTENT_SRC = "<content src=\""
    private val TAG_ORIG = "_orig"
    private val TAG_NEXT = "rel=\"next\""
    private val TAG_LINK_HREF = "<link href=\""

    val FILTER_PUBLISHED = 0;   val TAG_PUBLISHED = "<published>"
    val FILTER_UPDATED = 1;     val TAG_UPDATED = "<updated>"; val TAG_CLOSE_UPDATED = "</updated>"
    val FILTER_EDITED = 2
    val FILTER_CREATED = 3

    var nextHref: String? = null

    var isBottomReached = false

    fun parseResponse(response: String?): ParsedResponse{

        var entryTagIndex = response?.indexOf(TAG_ENTRY)

        var bufferString = response

        val imagesHrefs = ArrayList<String>()

        if (response?.contains(TAG_NEXT) == true) {
            nextHref = getNextHref(response)
        }else{

            isBottomReached = true
        }

        while (entryTagIndex != -1 && bufferString?.contains(TAG_CLOSE_ENTRY) == true){

            val entry = getEntry(bufferString)

            entry?.let{
                getImageHRefWithoutPostfix(it)?.let {
                    imagesHrefs.add(it)
                }
            }

            bufferString = bufferString.substring(bufferString.indexOf(TAG_CLOSE_ENTRY) + TAG_CLOSE_ENTRY.length)
            entryTagIndex = bufferString.indexOf(TAG_ENTRY)


        }

        return ParsedResponse(imagesHrefs, nextHref, isBottomReached)

    }

    class ParsedResponse(val imagesHrefs: ArrayList<String>,val nextHref: String?, val isBottomReached: Boolean){
    }

    fun getNextHref(response: String?): String?{

        val cutResponse = response?.substring(0, response.indexOf(TAG_NEXT))

        return cutResponse?.substring(
                cutResponse.lastIndexOf(TAG_LINK_HREF) + TAG_LINK_HREF.length,
                cutResponse.lastIndexOf('\"')
        )
    }

    fun getTime(entry: String?, filter: Int = FILTER_UPDATED): String?{
        return when (entry){
            null -> null
            else -> entry.substring(entry.indexOf(TAG_UPDATED) + TAG_UPDATED.length, entry.indexOf(TAG_CLOSE_UPDATED))
        }
    }


    private fun getImageHRefWithoutPostfix(entry: String): String?{

        val srcIndex = entry.indexOf(TAG_CONTENT_SRC)

        if (srcIndex == -1)
            return null

        val bufferString = entry.substring(srcIndex + TAG_CONTENT_SRC.length)
        val srcEndIndex = bufferString.indexOf(TAG_ORIG)

        return if (srcEndIndex != -1)
            bufferString.substring(0, srcEndIndex)
        else
            null
    }

    private fun getEntry(response: String): String?{

        val entryTagIndex = response.indexOf(TAG_ENTRY)
        val closeEntryTagIndex =  response.indexOf(TAG_CLOSE_ENTRY)

        return if (entryTagIndex > 0 && closeEntryTagIndex > 0)
            response.substring(entryTagIndex + TAG_ENTRY.length, closeEntryTagIndex)
        else null

    }
}