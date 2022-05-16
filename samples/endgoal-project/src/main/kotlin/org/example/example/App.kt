import guru.zoroark.tegral.web.*

fun main() = tegral {
    useConfiguration<Config>()

    put(::HelloController)
    put(::CrudController)

    put(::CrudService)

    put(::CrudRepository)
}
