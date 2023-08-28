package io.leafnode.gatling.solr

import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression
import io.leafnode.gatling.solr.protocol.SolrProtocolBuilder
import io.leafnode.gatling.solr.request.builder.SolrRequestBuilder

object Predef {
  def solr(implicit configuration: GatlingConfiguration) = SolrProtocolBuilder(configuration)

  def solr(requestName: Expression[String], collectionName: Expression[String]) = SolrRequestBuilder(requestName, collectionName)
}
