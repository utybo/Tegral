class Controller

class Service(scope: InjectionScope) {
    private val controller: Controller by scope()
}

tegralDi {
    put(::Controller)
    put(::Service)
}
