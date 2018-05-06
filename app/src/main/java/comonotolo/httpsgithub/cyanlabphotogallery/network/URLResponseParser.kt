package comonotolo.httpsgithub.cyanlabphotogallery.network


/**
 * Class that parses xml response of Yandex's server
 */
class URLResponseParser {

    private val TAG_ENTRY = "<entry>"
    private val TAG_CLOSE_ENTRY = "</entry>"

    private val TAG_CONTENT_SRC = "<content src=\""

    private val TAG_ORIG = "_orig"
    private val TAG_S = "_S"
    private val TAG_M = "_M"
    private val TAG_L = "_L"
    private val TAG_XL = "_XL"
    private val TAG_XXL = "_XXL"
    private val TAG_XXXL = "_XXXL"

    private val SUFFIXES = arrayOf(TAG_ORIG, TAG_XXXL, TAG_XXL, TAG_XL, TAG_L, TAG_M, TAG_S)

    private val TAG_NEXT = "rel=\"next\""
    private val TAG_LINK_HREF = "<link href=\""

    private val TAG_TITLE = "<title>"
    private val TAG_CLOSE_TITLE = "</title>"

    private var nextHref: String? = null

    private var isBottomReached = false

    private var imageNames = ArrayList<String?>()

    fun parseResponse(response: String?): ParsedResponse{

        var entryTagIndex = response?.indexOf(TAG_ENTRY)

        var bufferString = response

        val imagesHrefs = ArrayList<String?>()

        if (response?.contains(TAG_NEXT) == true) {

            nextHref = getNextHref(response)

        }else{

            isBottomReached = true
        }

        while (entryTagIndex != -1 && bufferString?.contains(TAG_CLOSE_ENTRY) == true){

            val entry = getEntry(bufferString)

            if (entry != null){
                val href = getImageHRefWithoutPostfix(entry)
                if (href != null){
                    imagesHrefs.add(href)
                    imageNames.add(getName(entry))
                }

            }

            bufferString = bufferString.substring(bufferString.indexOf(TAG_CLOSE_ENTRY) + TAG_CLOSE_ENTRY.length)
            entryTagIndex = bufferString.indexOf(TAG_ENTRY)


        }

        return ParsedResponse(imagesHrefs, imageNames, nextHref, isBottomReached)

    }

    class ParsedResponse(val imagesHrefs: ArrayList<String?>, val imagesNames: ArrayList<String?>, val nextHref: String?, val isBottomReached: Boolean)

    private fun getNextHref(response: String?): String? {

        val cutResponse = response?.substring(0, response.indexOf(TAG_NEXT))

        return cutResponse?.substring(
                cutResponse.lastIndexOf(TAG_LINK_HREF) + TAG_LINK_HREF.length,
                cutResponse.lastIndexOf('\"')
        )
    }

    private fun getName(entry: String?): String? {

        return entry?.substring(
                entry.indexOf(TAG_TITLE) + TAG_TITLE.length,
                entry.indexOf(TAG_CLOSE_TITLE)
        )
    }


    /**
     * All links are stored without postfix to request any sized image easily
     */
    private fun getImageHRefWithoutPostfix(entry: String): String?{

        val srcIndex = entry.indexOf(TAG_CONTENT_SRC)

        if (srcIndex == -1)
            return null

        val bufferString = entry.substring(srcIndex + TAG_CONTENT_SRC.length)
        var srcEndIndex = -1

        for (suffix in SUFFIXES){
            if (bufferString.contains(suffix)){
                srcEndIndex = bufferString.indexOf(suffix)
                break
            }
        }

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