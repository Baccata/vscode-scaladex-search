import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._
import scalajsbundler.util.JSON._

import scala.sys.process._

object VsCodeExtensionPlugin extends AutoPlugin {

  override def requires = ScalaJSPlugin && ScalaJSBundlerPlugin

  object autoImport {
    case class VsCommand(command: String, title: String)
    val contributedCommands = settingKey[Seq[VsCommand]]("Commands contributed by the extension.")
    val assemble = taskKey[Unit]("Assembles the extension and its NPM dependencies.")
    val open = taskKey[Unit]("Opens VS Code in the Extension Development Host mode.")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    assemble := {
      (Compile / npmInstallDependencies).value
      (Compile / fastOptJS).value
    },
    open := {
      assemble.value
      val base = (baseDirectory).value
      val log = (streams).value.log

      val path = base.getCanonicalPath + s"/target/scala-${(scalaBinaryVersion).value}/scalajs-bundler/main"
      s"code --verbose --extensionDevelopmentPath=${path}" ! log
      ()
    },
    contributedCommands := Seq(),
    Compile / fastOptJS / artifactPath := (Compile / fastOptJS / artifactPath).value.getParentFile / "extension.js",
    Compile / fullOptJS / artifactPath := (Compile / fullOptJS / artifactPath).value.getParentFile / "extension.js",
    Compile / additionalNpmConfig ++= Map(
      "name" -> str((ThisBuild / normalizedName).value),
      "version" -> str((ThisBuild / version).value),
      "displayName" -> str((ThisBuild / name).value),
      "description" -> str((ThisBuild / description).value),
      "publisher" -> str((ThisBuild / organization).value),
      "homepage" -> str((ThisBuild / homepage).value.get.toString),
      "repository" -> repositoryJson((ThisBuild / scmInfo).value.get),
      "license" -> str(toLicenseString((ThisBuild / licenses).value)),
      "categories" -> arr(str("Other")),
      "engines" -> obj("vscode" -> str((Compile / npmDevDependencies).value.toMap.apply("@types/vscode"))),
      "main" -> str("./extension.js"),
      "contributes" -> commandsJson(contributedCommands.value),
      "activationEvents" -> activationEventsJson(contributedCommands.value)
    )
  )

  private def activationEventsJson(commands: Seq[VsCommand]) = {
    arr(commands.map {
      case VsCommand(command, title) =>
        str(s"onCommand:${command}")
    }: _*)
  }

  private def commandsJson(commands: Seq[VsCommand]) = {
    obj("commands" -> arr(commands.map {
      case VsCommand(command, title) =>
        obj("command" -> str(command), "title" -> str(title))
    }: _*))
  }

  private def toLicenseString(licenses: Seq[(String, URL)]) = {
    if(licenses.isEmpty) "UNLICENSED" else licenses(0)._1
  }

  private def repositoryJson(scmInfo: ScmInfo) = {
    obj("type" -> str("git"), "url" -> str(scmInfo.connection.replaceFirst("^scm:", "")))
  }

}
