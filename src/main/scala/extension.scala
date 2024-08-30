import typings.vscode
import typings.vscode.anon.Dispose
import typings.vscode.mod.ExtensionContext

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

import typings.vscode.mod.QuickPickItem

object extension {
  import ops._

  @JSExportTopLevel("activate")
  def activate(context: ExtensionContext): Unit = {

    println(
      """Extension active !"""
    )

    def scaladexSearch(): js.Function1[scala.Any, scala.Any] = in => {
      val inputBox = vscode.mod.window.createQuickPick[QuickPickItem]()

      inputBox.placeholder = "name a scala library (eg: cats) and press enter"

      inputBox.onDidChangeSelection { items =>
        items.headOption match {
          case Some(value) =>
            for {
              p <- thenable(
                value
                  .asInstanceOf[js.Dynamic]
                  .selectDynamic("project")
                  .asInstanceOf[Project]
              )
              details <- scaladex.project(p.organization, p.repository)
              _ <- thenable(inputBox.dispose())
              _ <- thenable(artifacts(details))
            } yield ()
          case None => thenable(())
        }
      }

      inputBox.onDidAccept { () =>
        scaladex.search(inputBox.value).flatMap { projects =>
          thenable(inputBox.items = projects.map { project =>
            val item = QuickPickItem(project.repository)
            item.set("project", project)
            item.setDescription(s"from ${project.organization}")
            item
          })
        }
      }
      inputBox.show()
    }

    def artifacts(projectDetails: ProjectDetails): Unit = {
      val inputBox = vscode.mod.window.createQuickPick[QuickPickItem]()
      inputBox.canSelectMany = true
      inputBox.items = projectDetails.artifacts.map(a => QuickPickItem(a))

      var selection: js.Array[String] = js.Array()

      inputBox.onDidChangeSelection { newSelection =>
        thenable { selection = newSelection.map(_.label) }
      }

      inputBox.onDidAccept { _ =>
        val artifacts =
          if (!selection.isEmpty) {
            selection
          } else {
            inputBox.activeItems.map(_.label)
          }
        inputBox.dispose()
        versions(projectDetails.groupId, artifacts, projectDetails.versions)
      }

      inputBox.show()
    }

    def versions(
        groupId: String,
        artifacts: js.Array[String],
        versions: js.Array[String]
    ): Unit = {
      val inputBox = vscode.mod.window.createQuickPick[QuickPickItem]()
      inputBox.items = versions.map(v => QuickPickItem(v))

      inputBox.onDidChangeSelection { items =>
        items.headOption match {
          case Some(value) =>
            for {
              sbtFiles <- vscode.mod.workspace.findFiles("*.sbt")
              _ <- thenable(inputBox.dispose())
              _ <- save(groupId, artifacts, value.label, (sbtFiles.size > 0))
            } yield ()
          case None => thenable(())
        }
      }
      inputBox.show()
    }

    def save(
        groupId: String,
        artifacts: js.Array[String],
        version: String,
        sbtFileExists: Boolean
    ) = {
      val documentOpt = vscode.mod.window.activeTextEditor
        .map(_.document)

      val fileName = documentOpt.map(_.fileName)
        .getOrElse("build.sbt")

      val fileText = documentOpt.map(_.getText()).getOrElse("")

      val depString =
        if (fileName.endsWith(".sbt") || (fileName.endsWith(".scala") && sbtFileExists)) {
          artifacts
            .map(a => s""" "$groupId" %% "$a" % "$version" """.trim())
            .mkString(",\n")
        } else if (fileName.endsWith("build.sc")) {
          artifacts
            .map(a => s""" ivy"$groupId::$a:$version" """.trim())
            .mkString(",\n")
        } else if (fileName.endsWith(".scala") || isLikelyScalaCliScript(fileName, fileText)) {
          artifacts
            .map(a => s"""//> using dep "$groupId::$a:$version"""")
            .mkString("\n")
        } else if (fileName.endsWith("bleep.yaml")) {
          artifacts
            .map(a => s"""$groupId::$a:$version""")
            .mkString("\n")
        } else
          artifacts
            .map(a => s"""import $$ivy.`$groupId::$a:$version`""")
            .mkString("\n")
      for {
        _ <- vscode.mod.env.clipboard.writeText(depString)
        _ <- vscode.mod.window.showInformationMessage(
          "Dependencies have been copied to the clipboard"
        ).unit
      } yield ()
    }

    val commands = List(
      ("extension.baccata.scaladex", scaladexSearch())
    )

    commands.foreach {
      case (name, fun) =>
        context.subscriptions.push(
          vscode.mod.commands
            .registerCommand(name, fun)
            .asInstanceOf[Dispose]
        )
    }

  }

  private def isLikelyScalaCliScript(
    fileName: String,
    fileText: String
  ): Boolean = {
    fileName.endsWith(".sc") &&
    // optional spaces after //> are allowed
    """//> *using""".r.findFirstIn(fileText).isDefined
  }

}
