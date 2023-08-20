package io.leafnode.gatling.solr.protocol

import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}
import org.apache.solr.client.solrj.SolrClient


object SolrProtocol {

  def apply(configuration: GatlingConfiguration): SolrProtocol = SolrProtocol (
    solrClientProvider = null
  )

  val SolrProtocolKey = new ProtocolKey[SolrProtocol, SolrComponents] {

    type Protocol = SolrProtocol
    type Components = SolrComponents

    def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[SolrProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    def defaultProtocolValue(configuration: GatlingConfiguration): SolrProtocol = SolrProtocol(configuration)

    def newComponents(coreComponents: CoreComponents): SolrProtocol => SolrComponents = {

      solrProtocol => {
        val solrComponents = SolrComponents (
          solrProtocol
        )

        solrComponents
      }
    }
  }
}

case class SolrProtocol(solrClientProvider: () => SolrClient) extends Protocol {

  def solrClientProvider(solrClientProvider: () => SolrClient): SolrProtocol = copy(solrClientProvider = solrClientProvider)

}
