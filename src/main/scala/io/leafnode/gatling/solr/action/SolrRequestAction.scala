package io.leafnode.gatling.solr.action

import io.leafnode.gatling.solr.protocol.SolrProtocol
import io.leafnode.gatling.solr.request.builder.SolrAttributes
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.DefaultClock
import io.gatling.commons.validation.Validation
import io.gatling.core.CoreComponents
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session.Session
import io.gatling.core.util.NameGen
import org.apache.solr.client.solrj.{SolrClient, SolrServerException}
import java.io.IOException

class SolrRequestAction( val solrClient: SolrClient,
  val solrAttributes: SolrAttributes,
  val coreComponents: CoreComponents,
  val solrProtocol: SolrProtocol,
  val throttled: Boolean,
  val next: Action )
  extends ExitableAction with NameGen {

  val statsEngine = coreComponents.statsEngine
  val clock = new DefaultClock
  override val name = genName("solrRequest")

  override def execute(session: Session): Unit = recover(session) {

    solrAttributes requestName session flatMap { requestName =>

      val outcome =
        sendRequest(
          requestName,
          solrClient,
          solrAttributes,
          throttled,
          session)

      outcome.onFailure(
        errorMessage =>
          statsEngine.reportUnbuildableRequest(session.scenario, session.groups, requestName, errorMessage)
      )

      outcome

    }

  }

  private def sendRequest( requestName: String,
    solrClient: SolrClient,
    solrAttributes: SolrAttributes,
    throttled: Boolean,
    session: Session ): Validation[Unit] = {

    solrAttributes request session map { request =>

      val requestStartDate = clock.nowMillis
      val collectionName: String = solrAttributes.collectionName(session).toOption.get

      var e: Exception = null
      try {
        request.process(solrClient, collectionName)
      } catch {
        case solrEx: SolrServerException => e = solrEx
        case ioEx: IOException => e = ioEx
      }

      val requestEndDate = clock.nowMillis
      statsEngine.logResponse(
        scenario = session.scenario,
        groups = session.groups,
        requestName = requestName,
        startTimestamp = requestStartDate,
        endTimestamp = requestEndDate,
        status = if (e == null) OK else KO,
        responseCode = None,
        message = if (e == null) None else Some(e.getMessage)
      )

      coreComponents.throttler match {
        case Some(th) if throttled =>  th.throttle(session.scenario, () => next ! session)
        case _ => next ! session
      }

    }

  }

}
