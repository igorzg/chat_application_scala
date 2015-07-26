package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import scala.reflect.io.Path
import scala.io.Source
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
 * Class package
 * @param name
 * @param filePath
 * @param packages
 */
case class Package(
                    name: String,
                    controller: Option[String] = null,
                    var isVendor: Option[Boolean] = null,
                    deps: Option[Seq[String]] = null,
                    file: Option[String] = null,
                    varName: Option[String] = null,
                    var template: Option[String] = null,
                    var filePath: Option[String] = null,
                    packages: Option[Seq[Package]] = null
                    )

object RequireConfig extends Controller {

  /**
   * Lazy reads for recursive package
   */
  implicit lazy val packageReads: Reads[Package] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "controller").readNullable[String] and
      (JsPath \ "isVendor").readNullable[Boolean] and
      (JsPath \ "deps").readNullable[Seq[String]] and
      (JsPath \ "file").readNullable[String] and
      (JsPath \ "varName").readNullable[String] and
      (JsPath \ "tempalte").readNullable[String] and
      (JsPath \ "filePath").readNullable[String] and
      (JsPath \ "packages").lazyReadNullable(Reads.seq[Package](packageReads))
    )(Package.apply _)
  /**
   * Lazy write for recursive package config
   */
  implicit lazy val packageWrites: Writes[Package] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "controller").writeNullable[String] and
      (JsPath \ "isVendor").writeNullable[Boolean] and
      (JsPath \ "deps").writeNullable[Seq[String]] and
      (JsPath \ "file").writeNullable[String] and
      (JsPath \ "varName").writeNullable[String] and
      (JsPath \ "tempalte").writeNullable[String] and
      (JsPath \ "filePath").writeNullable[String] and
      (JsPath \ "packages").lazyWriteNullable(Writes.seq[Package](packageWrites))
    )(unlift(Package.unapply))

  /**
   * Find all packages and create config for require js
   * @return
   */
  def findPackages: Array[Package] = {
    val defFile: String = "def.json"
    val assetsPath: String = "/assets/js/"
    val appPath: String = Play.application.path.toString() + "/app"
    val path: String = appPath + assetsPath
    var paths: Array[Package] = Array[Package]()

    val values = Path(path) walkFilter { p =>
      p.isDirectory || defFile.r.findFirstIn(p.name).isDefined
    }

    values.toList.foreach(i => {
      val file: Package = Json.parse(Source.fromFile(i.path).getLines().mkString).as[Package]
      file.filePath = Option(i.path.replace(appPath, "").replace(defFile, ""))
      paths = paths :+ file
    })

    paths
  }

  /**
   * Handle config action
   * @return
   */
  def config = Action { request =>
    var script: String = "define(function config() {\n"
    script += "return " + Json.prettyPrint(Json.toJson(findPackages)) + ";"
    script += "\n})"
    Ok(script).as("application/javascript");
  }
}
