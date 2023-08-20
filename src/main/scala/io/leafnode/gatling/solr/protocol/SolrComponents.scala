package io.leafnode.gatling.solr.protocol

import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session


case class SolrComponents(solrProtocol: SolrProtocol) extends ProtocolComponents {

  override def onStart: Session => Session = Session.Identity

  override def onExit: Session => Unit = ProtocolComponents.NoopOnExit

}
