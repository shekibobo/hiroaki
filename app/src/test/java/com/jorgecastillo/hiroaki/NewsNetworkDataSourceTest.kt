package com.jorgecastillo.hiroaki

import com.jorgecastillo.hiroaki.model.Article
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class NewsNetworkDataSourceTest {

    private lateinit var dataSource: NewsNetworkDataSource
    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        val service = server.mockService(NewsApiService::class.java)
        dataSource = NewsNetworkDataSource(service)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun sendsGetNewsToRequiredEndpoint() {
        server.enqueueSuccessfulResponse("GetNews.json")

        runBlocking { dataSource.getNews() }

        server.assertRequest(
                sentToPath = "v2/top-headlines",
                queryParams = listOf(
                        "sources" to "crypto-coins-news",
                        "apiKey" to "a7c816f57c004c49a21bd458e11e2807"))
    }

    @Test
    fun parsesNewsProperly() {
        server.enqueueSuccessfulResponse("GetNews.json")

        val news = runBlocking { dataSource.getNews() }

        thenNewsAreParsed(news)
    }

    @Test(expected = IOException::class)
    fun throwsIOExceptionOnGetNewsErrorResponse() {
        server.enqueueErrorResponse()

        runBlocking { dataSource.getNews() }
    }

    private fun thenNewsAreParsed(news: List<Article>) {
        assertThat(news.size, `is`(3))
    }
}
