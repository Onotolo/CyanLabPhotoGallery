package comonotolo.httpsgithub.cyanlabphotogallery

import comonotolo.httpsgithub.cyanlabphotogallery.network.URLResponseParser
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun imagesTimeCorrect(){
        assertEquals("http://api-fotki.yandex.ru/api/users/alekna/album/63988/photos/updated;2009-01-27T11:57:19Z,126744/", URLResponseParser().getNextHref("<feed xmlns=\"http://www.w3.org/2005/Atom\" xmlns:app=\"http://www.w3.org/2007/app\" xmlns:f=\"yandex:fotki\">\n" +
                "  <id>urn:yandex:fotki:alekna:album:63988:photos</id>\n" +
                "  <author>\n" +
                "    <name>alekna</name>\n" +
                "    <f:uid>14733932</f:uid>\n" +
                "  </author>\n" +
                "  <title>Каникулы в Германии</title>\n" +
                "  <updated>2009-01-27T11:57:33Z</updated>\n" +
                "  <summary>Наша школьная поездка в Германию</summary>\n" +
                "  <link href=\"http://api-fotki.yandex.ru/api/users/alekna/album/63988/photos/\" rel=\"self\" />\n" +
                "  <link href=\"http://fotki.yandex.ru/users/alekna/album/63988/\" rel=\"alternate\" />\n" +
                "  <f:image-count value=\"10\" />\n" +
                "  <!-- Данная ссылка содержит адрес следующей страницы выдачи: -->\n" +
                "  <link href=\"http://api-fotki.yandex.ru/api/users/alekna/album/63988/photos/updated;2009-01-27T11:57:19Z,126744/\" rel=\"next\" />\n" +
                "  <entry>...</entry>\n" +
                "  <entry>...</entry>\n" +
                "  ...\n" +
                "  <entry>...</entry>\n" +
                "</feed>"))
    }
}
