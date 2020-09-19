import typings.vscode.Thenable
import scala.scalajs.js
import js.|

object ops {

  def thenable[A](a: A): Thenable[A] = {
    try {
      js.Promise.resolve[A](a).asInstanceOf[Thenable[A]]
    } catch {
      case e: Throwable => js.Promise.reject(e).asInstanceOf[Thenable[A]]
    }
  }

  implicit def funConverter[A](f: () => A): js.Function1[Unit, A] =
    (_: Unit) => f()

  implicit class ThenableOps[A](private val p: Thenable[A]) extends AnyVal {

    def flatMap[B](f: A => Thenable[B]): Thenable[B] =
      p.`then`[B]((a: A) => (f(a): B | Thenable[B]))

    def map[B](f: A => B): Thenable[B] =
      p.`then`[B]((a: A) => (f(a): B | Thenable[B]))

    def toFuture: scala.concurrent.Future[A] = {
      val p2 = scala.concurrent.Promise[A]()
      p.`then`[Unit](
        onfulfilled = { (v: A) =>
          p2.success(v)
          (): Unit | Thenable[Unit]
        },
        onrejected = { (e: js.Any) =>
          p2.failure(e match {
            case th: Throwable => th
            case _             => js.JavaScriptException(e)
          })
          (): Unit | Thenable[Unit]
        }: js.Function1[js.Any, Unit | Thenable[Unit]]
      )
      p2.future
    }
  }
}
