data class Greeting {
    val recipient: String
}

get("/hello") {
    call.respond(Greeting("You"))
} describe {
    summary = "Greet someone"
    200 response {
        json { schema<Greeting>(Greeting("Hi!")) }
    }
}
