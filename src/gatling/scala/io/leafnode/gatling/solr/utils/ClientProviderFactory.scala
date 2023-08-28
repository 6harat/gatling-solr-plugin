package io.leafnode.gatling.solr.utils

import java.util
import org.apache.http.{HttpHeaders, HttpRequest, HttpRequestInterceptor}
import org.apache.http.client.config.RequestConfig
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.{ConnectionSocketFactory, PlainConnectionSocketFactory}
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.{DecompressingHttpClient, HttpClients}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.protocol.HttpContext
import org.apache.solr.client.solrj.impl.{BinaryRequestWriter, BinaryResponseParser, CloudSolrClient, Http2SolrClient, HttpSolrClient}

import java.util.concurrent.TimeUnit

object ClientProviderFactory {

  val httpClientFactoryProvider = (baseUrl: String, maxConnections: Int, maxConnectionsPerRoute: Int,
  solrConnectTimeoutMs: Int, solrSocketTimeoutMs: Int) => () => {
    val httpConnectionFactory = RegistryBuilder.create[ConnectionSocketFactory]
      .register("http", PlainConnectionSocketFactory.getSocketFactory)
      .register("https", SSLConnectionSocketFactory.getSocketFactory)
      .build
    val httpClientConnectionManager = new PoolingHttpClientConnectionManager(httpConnectionFactory)
    httpClientConnectionManager.setMaxTotal(maxConnections)
    httpClientConnectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
    val requestConfig = RequestConfig.custom
      .setConnectTimeout(solrConnectTimeoutMs)
      .setSocketTimeout(solrSocketTimeoutMs)
      .build
    val httpClientBuilder = HttpClients.custom.disableContentCompression.setConnectionManager(httpClientConnectionManager)
      .setMaxConnPerRoute(maxConnectionsPerRoute).setDefaultRequestConfig(requestConfig)
    httpClientBuilder.addInterceptorLast(new HttpRequestInterceptor() {
      override def process(request: HttpRequest, context: HttpContext): Unit = {
        request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
      }
    })
    httpClientBuilder.setMaxConnTotal(maxConnections)

    val httpClient = new DecompressingHttpClient(httpClientBuilder.build)
    val solrClient = new HttpSolrClient.Builder().withBaseSolrUrl(baseUrl)
      .withHttpClient(httpClient)
      .withConnectionTimeout(solrConnectTimeoutMs)
      .withSocketTimeout(solrSocketTimeoutMs)
      .withRequestWriter(new BinaryRequestWriter)
      .withResponseParser(new BinaryResponseParser)
      .build()
    solrClient
  }

  val http2ClientFactoryProvider = (baseUrl: String, maxConnectionsPerHost: Int, solrConnectTimeoutMs: Int, solrRequestTimeoutMs: Int) => () => {
    val solrClient = new Http2SolrClient.Builder(baseUrl)
      .withMaxConnectionsPerHost(maxConnectionsPerHost)
      .withConnectionTimeout(solrConnectTimeoutMs, TimeUnit.MILLISECONDS)
      .withIdleTimeout(10, TimeUnit.MINUTES)
      .withRequestTimeout(solrRequestTimeoutMs, TimeUnit.MILLISECONDS)
      .withRequestWriter(new BinaryRequestWriter)
      .withResponseParser(new BinaryResponseParser)
      .build()
    solrClient
  }

  val solrCloudClientFactoryProvider = (baseUrl: String, maxConnectionsPerHost: Int, solrConnectTimeoutMs: Int,
  solrRequestTimeoutMs: Int) => () => {
    val http2ClientBuilder = new Http2SolrClient.Builder()
      .withMaxConnectionsPerHost(maxConnectionsPerHost)
      .withConnectionTimeout(solrConnectTimeoutMs, TimeUnit.MILLISECONDS)
      .withIdleTimeout(10, TimeUnit.MINUTES)
      .withRequestTimeout(solrRequestTimeoutMs, TimeUnit.MILLISECONDS)
      .withRequestWriter(new BinaryRequestWriter)
      .withResponseParser(new BinaryResponseParser)

    val solrClient = new CloudSolrClient.Builder(util.List.of(baseUrl))
      .withInternalClientBuilder(http2ClientBuilder)
      .withRetryExpiryTime(10, TimeUnit.SECONDS)
      .build()
    solrClient
  }

}
