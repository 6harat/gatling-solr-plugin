package io.leafnode.gatling.solr.action

import io.leafnode.gatling.solr.protocol.{SolrComponents, SolrProtocol}
import io.leafnode.gatling.solr.request.builder.SolrAttributes
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext


class SolrRequestActionBuilder(solrAttributes: SolrAttributes) extends ActionBuilder {

  override def build( ctx: ScenarioContext, next: Action ): Action = {
    import ctx.{protocolComponentsRegistry, coreComponents, throttled}

    val solrComponents: SolrComponents = protocolComponentsRegistry.components(SolrProtocol.SolrProtocolKey)

    val solrClient = solrComponents.solrProtocol.solrClientProvider()

    coreComponents.actorSystem.registerOnTermination(solrClient.close())

    new SolrRequestAction(
      solrClient,
      solrAttributes,
      coreComponents,
      solrComponents.solrProtocol,
      throttled,
      next
    )

  }

}