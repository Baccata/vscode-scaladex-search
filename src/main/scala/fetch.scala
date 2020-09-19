import typings.vscode.Thenable

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@JSImport("node-fetch", JSImport.Default)
@js.native
object fetch extends js.Function1[String, Thenable[FetchResponse]] {
  override def apply(arg1: String): Thenable[FetchResponse] = js.native
}

trait FetchResponse extends js.Object {
  def status: Int
  def json(): Thenable[js.Any]
  def text(): Thenable[String]
}
