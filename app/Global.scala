import play.api._
import play.api.libs.Codecs._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import play.api.libs.Codecs.sha1

object Global extends GlobalSettings {

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {
    val UID = "session_id"
    val sesionCookie: Option[Cookie] = request.cookies.get(UID)
    var sesion_id : String = null
    if (sesionCookie.isDefined) {
      sesion_id = sesionCookie.get.value
    } else {
      sesion_id = sha1(request.session + UID)
    }
    Future.successful(Ok(views.html.index(request, sesion_id)))
  }
}