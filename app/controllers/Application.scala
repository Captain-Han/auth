package controllers

import play.api._
import play.api.mvc._

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
}