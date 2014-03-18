package controllers

import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import models._
import java.util.Date
import com.mongodb.casbah.WriteConcern
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import play.api.data.Form
import play.api.data.Forms._
import jp.t2v.lab.play2.auth._
import jp.t2v.lab.play2.stackc.{RequestWithAttributes, RequestAttributeKey, StackableController}
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import reflect.{ClassTag, classTag}
import play.api.mvc.RequestHeader

object Users extends Controller with LoginLogout with AuthElement with AuthConfigImpl {
  val registerForm: Form[User] = Form(
    mapping(
      "username" -> text,
      // Create a tuple mapping for the password/confirm
      "password" -> tuple(
        "main" -> text,
        "confirm" -> text).verifying(
          // Add an additional constraint: both passwords must match
          "Passwords don't match", passwords => passwords._1 == passwords._2),
      "permission" -> text) {
        // Binding: Create a User from the mapping result (ignore the second password and the accept field)
        (username, password, permission) => User(new ObjectId, username, password._1, permission)
      } // Unbinding: Create the mapping values from an existing Hacker value
      {
        user => Some((user.username, (user.password, ""), user.permission))
      } /*.verifying(
        // Add an additional constraint: The username must not be taken (you could do an SQL request here)
        "This username is not available",
        username => !Seq("admin", "guest").contains(username)))*/ )
  val loginForm = Form(mapping(
    "username" -> nonEmptyText,
    "password" -> nonEmptyText)(User.authenticate)(_.map(u => (u.username, "")))
      .verifying("Invalid email or password", result => result.isDefined)
    
  )

 val userForm: Form[User] = Form(
    mapping(
      "username" -> text,
      "password" -> text,
      "permission" -> text) {
        // Binding: Create a User from the mapping result (ignore the second password and the accept field)
        (username, password, permission) => User(new ObjectId, username, password, permission)
      } // Unbinding: Create the mapping values from an existing Hacker value
      {
        user => Some((user.username, user.password, user.permission))
      }
  )
  
  def index() = Action {
    val users = User.findAll().toList
    Ok("")
  }

  /*def login() = JsonAction[User] { user =>
    val u = User.authenticate(user.username, user.password).get
    Ok(views.html.success(u.username))
  }*/

  def login = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.login(formWithErrors))),
      user           => gotoLoginSucceeded(user.get.username)
    )
  }

  /* def register() = JsonAction[User] { user =>
    User.save(user, WriteConcern.Safe)
    Ok(Json.toJson(user))
    //Ok(views.html.success(user.username))
  }*/
  def register = Action { implicit request =>
    Users.registerForm.bindFromRequest.fold(
      errors => BadRequest(views.html.message1(errors)),
      {
        user =>
          User.save(user, WriteConcern.Safe)
          Ok(views.html.success(user.username))
      })
  }

  def update(id: ObjectId) = Action { implicit request =>
    Users.userForm.bindFromRequest.fold(
      errors => BadRequest(views.html.message1(errors)),
      {
        user =>
          User.save(user.copy(id = id), WriteConcern.Safe)
          Ok(views.html.success(user.username))
      })
  }

  def showUser(username: String) = StackAction(AuthorityKey -> authorization(Administrator) _) {implicit request =>
  	val user = loggedIn
    val userForm = Users.userForm.fill(User.findOneByUsername(username).get)
    Ok(views.html.UserInfomation(userForm))
    }
  
  def show() = StackAction(AuthorityKey -> authorization(NormalUser) _) {implicit request =>
  	val user = loggedIn
    val userForm = Users.userForm.fill(user)
    Ok(views.html.UserInfomation(userForm))
    }
  
  def edit(username: String) = StackAction(AuthorityKey -> isAuthor(username) _) {implicit request =>
    val user = loggedIn
    val userForm = Users.userForm.fill(User.findOneByUsername(username).get)
    Ok(views.html.UserInfomation(userForm))
    }
  
  //def isAuthor(id :ObjectId, objectType : String)(user : User) : Future[Boolean] =
    def isAuthor(username : String)(user : User) : Future[Boolean] =
    Future{User.findOneByUsername(username).map(_ == user).get}
  
  def requireAdminUser(user: User): Future[Boolean] = Future.successful(user.permission == "Administrator")
  def authorization(permission: Permission)(user : User)(implicit ctx: ExecutionContext) = Future.successful((permission, user.permission) match {
    case ( _, "Administrator") => true
    case (NormalUser, "NormalUser") => true
    case _ => false
  })
}

trait AuthConfigImpl extends AuthConfig {

  type Id = String

  type User = models.User

  type Authority = User => Future[Boolean]

  val idTag: ClassTag[Id] = classTag[Id]

  val sessionTimeoutInSeconds = 3600

  def resolveUser(username: Id)(implicit ctx: ExecutionContext) = Future.successful(User.findOneByUsername(username))

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) = {
    val uri = request.session.get("access_uri").getOrElse(routes.Users.show.url.toString)
    Future.successful(Redirect(uri).withSession(request.session - "access_uri"))
  }
  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) = Future.successful(Redirect(routes.Application.login))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext) =
		  Future.successful(Redirect(routes.Application.login).withSession("access_uri" -> request.uri))

  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext) = Future.successful(Forbidden("no permission"))

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] = authority(user)

}