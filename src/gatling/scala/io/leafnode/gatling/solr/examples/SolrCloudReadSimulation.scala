package io.leafnode.gatling.solr.examples

import java.util
import io.gatling.core.Predef._

import scala.concurrent.duration._
import io.leafnode.gatling.solr.Predef._
import io.leafnode.gatling.solr.utils.ClientProviderFactory
import org.apache.solr.client.solrj.SolrRequest
import org.apache.solr.client.solrj.request.QueryRequest
import org.apache.solr.common.params.MapSolrParams

class SolrCloudReadSimulation extends Simulation {
  val baseUrl = "http://localhost:8983/solr/"
  val solrInternalClient = ClientProviderFactory.solrCloudClientFactoryProvider(
    baseUrl,
    100, // max connections
    5000, // connect timeout
    2000) // socket timeout

  val solrConf = solr.solrClientProvider(solrInternalClient)

  val query = new QueryRequest(new MapSolrParams(util.Map.of("q", "*:*")), SolrRequest.METHOD.POST)

  val scn = scenario("Solr Http Read Test")
    .exec(
      solr("all-match", "techproducts")
        // query to send
        .send(query))

  setUp(
    scn
      .inject(constantUsersPerSec(10) during (90.seconds)))
    .protocols(solrConf)
}
