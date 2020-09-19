import scala.sys.process._

lazy val installDependencies = Def.task[Unit] {
  val base = (baseDirectory in ThisProject).value
  val log = (streams in ThisProject).value.log
  if (!(base / "node_module").exists) {
    val pb =
      new java.lang.ProcessBuilder("npm", "install")
        .directory(base)
        .redirectErrorStream(true)

    pb ! log
  }
}

lazy val open = taskKey[Unit]("open vscode")
def openVSCodeTask: Def.Initialize[Task[Unit]] =
  Def
    .task[Unit] {
      val base = (baseDirectory in ThisProject).value
      val log = (streams in ThisProject).value.log

      val path = base.getCanonicalPath
      s"code --extensionDevelopmentPath=$path" ! log
      ()
    }
    .dependsOn(installDependencies)

lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "2.13.3",
    moduleName := "vscode-scalajs-hello",
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    artifactPath in (Compile, fastOptJS) := baseDirectory.value / "out" / "extension.js",
    artifactPath in (Compile, fullOptJS) := baseDirectory.value / "out" / "extension.js",
    open := openVSCodeTask.dependsOn(fastOptJS in Compile).value,
    // scalaJSUseMainModuleInitializer := true,
    npmDependencies in Compile ++= Seq(
      "@types/vscode" -> "1.49",
      "node-fetch" -> "^2.6.1"
    ),
    stIgnore ++= List(
      "node-fetch"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
    // publishMarketplace := publishMarketplaceTask.dependsOn(fullOptJS in Compile).value
  )
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin,
    ScalablyTypedConverterPlugin
  )
