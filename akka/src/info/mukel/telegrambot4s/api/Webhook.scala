package info.mukel.telegrambot4s.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import info.mukel.telegrambot4s.methods.SetWebhook
import info.mukel.telegrambot4s.models.{InputFile, Update}
import slogging.StrictLogging

import scala.concurrent.Future
import scala.util.control.NonFatal

/** Uses a webhook, as an alternative to polling, to receive updates.
  *
  * Automatically registers the webhook on run().
  */
trait Webhook extends WebRoutes with StrictLogging {
  _: BotBase with BotExecutionContext with AkkaImplicits =>

  import info.mukel.telegrambot4s.marshalling.CirceMarshaller._
  import info.mukel.telegrambot4s.marshalling.AkkaHttpMarshalling._

  /** URL for the webhook.
    *
    * 'webhookUrl' must be consistent with 'webhookRoute' (by default '/').
    */
  val webhookUrl: String

  /**
    * Webhook route.
    *
    * 'webhookUrl/' by default.
    *
    * @return Route handler to process updates.
    */
  def webhookRoute: Route = pathEndOrSingleSlash(webhookReceiver)

  /**
    * Specify self-signed certificate file.
    * Check instructions at [[https://core.telegram.org/bots/self-signed Using self-signed certificates]].
    *
    * @return
    */
  def certificate: Option[InputFile] = None

  def webhookReceiver: Route = {
    entity(as[Update]) { update =>
      try {
        receiveUpdate(update)
      } catch {
        case NonFatal(e) =>
          logger.error("Caught exception in update handler", e)
      }
      complete(StatusCodes.OK)
    }
  }

  abstract override def routes: Route = webhookRoute ~ super.routes

  abstract override def run(): Future[Unit] = {
    request(
      SetWebhook(
        url = webhookUrl,
        certificate = certificate,
        allowedUpdates = allowedUpdates)).flatMap {
      case true => super.run() // spawn WebRoutes
      case false =>
        logger.error("Failed to set webhook")
        throw new RuntimeException("Failed to set webhook")
    }
  }
}
