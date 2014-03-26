package controllers

import play.api._
import play.api.mvc._
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.gridfs.GridFS
import se.radley.plugin.salat.Binders._
import scala.concurrent.ExecutionContext
import java.text.SimpleDateFormat
import play.api.libs.iteratee.Enumerator

object Application extends Controller {

  def index() = Action {
    Redirect(routes.Application.login)
  }

  def login() = Action { implicit request =>
    Ok(views.html.login(Users.loginForm))
  }

  def register() = Action {
    Ok(views.html.register(Users.registerForm))
  }
  
  def imgs() = Action {
    Ok(views.html.imgs())
  }
  
  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("photo") match {
      case Some(photo) =>
       val db = MongoConnection()("mydb")
       val gridFs = GridFS(db)
        val uploadedFile = gridFs.createFile(photo.ref.file)
        uploadedFile.contentType = photo.contentType.orNull
        uploadedFile.save()
        Redirect(routes.Users.saveImg(uploadedFile._id.get))
      case None => BadRequest("no photo")
    }
  }
  
  def getPhoto(file: ObjectId) = Action {
    import com.mongodb.casbah.Implicits._
    import ExecutionContext.Implicits.global
    
    val db = MongoConnection()("mydb")
    val gridFs = GridFS(db)

    gridFs.findOne(Map("_id" -> file)) match {
      case Some(f) => SimpleResult(
        ResponseHeader(OK, Map(
          CONTENT_LENGTH -> f.length.toString,
          CONTENT_TYPE -> f.contentType.getOrElse(BINARY),
          DATE -> new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", java.util.Locale.CHINA).format(f.uploadDate)
        )),
        Enumerator.fromStream(f.inputStream)
      )

      case None => NotFound
    }
  }


}