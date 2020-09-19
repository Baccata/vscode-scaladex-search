import scala.scalajs.js
import typings.std.stdStrings.p

object scaladex {

  import ops._

  val target = "JVM"
  val scalaVersion = "2.13"

  def search(name: String) = {
    val n = name.trim()
    fetch(
      s"https://index.scala-lang.org/api/search?q=$n&target=$target&scalaVersion=$scalaVersion"
    ).flatMap(_.json).map { json =>
      json.asInstanceOf[js.Array[Project]].filter { project =>
        project.repository.contains(n) || project.artifacts
          .exists(_.contains(n))
      }
    }
  }

  def project(org: String, repo: String) = {
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
