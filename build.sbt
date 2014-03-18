name := "auth"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)     

libraryDependencies += "org.mongodb" %% "casbah" % "2.6.5"

libraryDependencies += "com.novus" %% "salat" % "1.9.5"

libraryDependencies += "se.radley" %% "play-plugins-salat" % "1.3.0"

libraryDependencies += "jp.t2v" %% "play2-auth"      % "0.11.0"

libraryDependencies += "jp.t2v" %% "play2-auth-test" % "0.11.0" % "test"

play.Project.playScalaSettings

routesImport += "se.radley.plugin.salat.Binders._"
    
templatesImport += "org.bson.types.ObjectId"