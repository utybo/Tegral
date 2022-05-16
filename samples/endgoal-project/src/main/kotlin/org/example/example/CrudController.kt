class CrudController(scope: InjectionScope) : TegralController() {

    override fun Routing.wrapIn() {
        route("/crud") { super.wrapIn() }
    }

    override fun Routing.install() {
        route("/crud") {
            get {
                service.getElement
            }

            get("{id}") {
                val id = call.parameters["id"]
                service.getElementById()
            }

            post {

            }

            put {

            }


        }
    }
}
