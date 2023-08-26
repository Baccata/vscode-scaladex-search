import scala.scalajs.js
import typings.std.stdStrings.p
import typings.vscode.Thenable

object scaladex {

  import ops._

  val target = "JVM"
  val scalaVersion = Seq("2.13", "3")

  def search(name: String): Thenable[js.Array[Project]] = {
    val n = name.trim()

    val searchResults = scalaVersion.map { version =>
      fetch(
        s"https://index.scala-lang.org/api/search?q=$n&target=$target&scalaVersion=$version"
      ).flatMap(_.json).map { json =>
        json.asInstanceOf[js.Array[Project]].filter { project =>
          project.repository.contains(n) || project.artifacts
            .exists(_.contains(n))
        }
      }
    }

    searchResults.reduce { (a, b) =>
      a.flatMap { a =>
        b.map { b =>
          (a ++ b).distinctBy(_.repository)
        }
      }
    }
  }

  def project(org: String, repo: String): Thenable[ProjectDetails] = {
    fetch(
      s"https://index.scala-lang.org/api/project?organization=$org&repository=$repo"
    ).flatMap(_.json).map { json =>
      json.asInstanceOf[ProjectDetails]
    }
  }

}

@js.native
trait Project extends js.Object {
  def organization: String = js.native
  def repository: String = js.native
  def artifacts: js.Array[String] = js.native
}

@js.native
trait ProjectDetails extends js.Object {
  def artifacts: js.Array[String]
  def versions: js.Array[String]
  def groupId: String
  def version: String
}
