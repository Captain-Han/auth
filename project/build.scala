import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "auth"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "se.radley" %% "play-plugins-salat" % "1.3.0",
    "jp.t2v" %% "play2-auth"      % "0.11.0",
    "jp.t2v" %% "play2-auth-test" % "0.11.0" % "test"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    routesImport += "se.radley.plugin.salat.Binders._",
    templatesImport += "org.bson.types.ObjectId"
  )

}