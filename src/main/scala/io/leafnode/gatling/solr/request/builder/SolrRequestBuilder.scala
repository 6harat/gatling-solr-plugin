package io.leafnode.gatling.solr.request.builder

import io.leafnode.gatling.solr.action.SolrRequestActionBuilder
import io.gatling.core.session.Expression
import org.apache.solr.client.solrj.SolrRequest


case class SolrAttributes(requestName: Expression[String],
  collectionName: Expression[String],
  request: Expression[SolrRequest[_]])

case class SolrRequestBuilder(requestName: Expression[String], collectionName: Expression[String]) {

  def send(request: Expression[SolrRequest[_]]): SolrRequestActionBuilder =
    new SolrRequestActionBuilder(SolrAttributes(requestName, collectionName, request))

}
