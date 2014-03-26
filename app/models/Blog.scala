package models

import play.api.Play.current
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import mongoContext._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Blog(
    id : ObjectId = new ObjectId,
    title : String, 
    content : String)

object Blog extends ModelCompanion[Blog, ObjectId] {
  val dao = new SalatDAO[Blog, ObjectId](collection = mongoCollection("Blog")) {}

}
