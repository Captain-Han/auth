package controllers

import play.api.mvc._
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.gridfs.GridFS
import se.radley.plugin.salat.Binders._
import scala.concurrent.ExecutionContext
import java.text.SimpleDateFormat
import play.api.libs.iteratee.Enumerator
import play.api.mvc.SimpleResult
import models._
import java.io._
import play.api.mvc.ResponseHeader
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import play.api.data.Form
import play.api.data.Forms._
import play.api.templates.Html

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
  
  def upload() = Action(parse.multipartFormData) {implicit request =>
    request.body.file("photo").map{ photo =>
          imgForm.bindFromRequest.fold(
           errors =>Ok(Html(errors.toString)),
          img =>{
          val db = MongoConnection()("mydb")
          val gridFs = GridFS(db)
          val file = photo.ref.file

          val originImage =  ImageIO.read(file)

          val newImage = originImage.getSubimage(img.x1.intValue,img.y1.intValue,img.w.intValue,img.h.intValue)

          val  os = new ByteArrayOutputStream();

          ImageIO.write(newImage, "jpg", os);

          val inputStream = new ByteArrayInputStream(os.toByteArray());

          val uploadedFile = gridFs.createFile(inputStream)

          uploadedFile.contentType = photo.contentType.orNull
          uploadedFile.save()
          Redirect(routes.Users.saveImg(uploadedFile._id.get))
          }
          )
    }.getOrElse(Ok(Html("无图片")))
  }

    val imgForm : Form[Img] =Form(
        mapping(
        "x1"->bigDecimal,
        "y1"->bigDecimal,
        "x2"->bigDecimal,
        "y2"->bigDecimal,
        "w"->bigDecimal,
        "h"->bigDecimal)(Img.apply)(Img.unapply)
    )


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