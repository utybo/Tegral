// NOTE: this syntax is not available yet.

fun Routing.greeter() {
    get("/hello") {
      call.respond("Hello World!")
    }
}

fun main() {
  tegral {
    put(Routing::greeter)
  }
}
