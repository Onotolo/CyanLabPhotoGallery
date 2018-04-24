package comonotolo.httpsgithub.cyanlabphotogallery.network

class URLRequestMaker {

     fun getNewImages(limit: Int): String{
        return "GET /api/recent/?limit={" + limit.toShort() + "} HTTP/1.1\n" +
                "Host: api-fotki.yandex.ru\n" +
                "Accept: application/atom+xml; type=entry"
    }
}