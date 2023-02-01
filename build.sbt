ThisBuild / organization := "baccata"
ThisBuild / version      := "0.3.4"
ThisBuild / scalaVersion := "2.13.11"

ThisBuild / name := "Scaladex search"
ThisBuild / normalizedName := "scaladex-search"
ThisBuild / description := "Looks up Scala libraries from vscode"
ThisBuild / homepage := Some(url("https://github.com/Baccata/vscode-scaladex-search"))
ThisBuild / licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/Baccata/vscode-scaladex-search"),
    "scm:https://github.com/Baccata/vscode-scaladex-search.git"
  )
)


lazy val root = project
  .in(file("."))
  .settings(
    moduleName := "vscode-scaladex-search",
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
    // scalaJSUseMainModuleInitializer := true,
    Compile / npmDependencies ++= Seq(
      "node-fetch" -> "^2.6.1"
    ),
    Compile / npmDevDependencies ++= Seq(
      "@types/vscode" -> "^1.73.0"
    ),
    stIncludeDev := true,
    stIgnore ++= List(
      "node-fetch"
    ),
    contributedCommands += VsCommand("extension.baccata.scaladex", "Scaladex search"),
    testFrameworks += new TestFramework("utest.runner.Framework")
    // publishMarketplace := publishMarketplaceTask.dependsOn(fullOptJS in Compile).value
  )
  .enablePlugins(
    ScalaJSPlugin,
    ScalaJSBundlerPlugin,
    ScalablyTypedConverterPlugin,
    VsCodeExtensionPlugin
  )
