# Modules

Tegral DI provides a module system that allows you to define your environment in multiple, smaller bits. For example:

```kotlin
class UserRepository
class UserService(scope: InjectionScope) {
    private val repository: UserRepository by scope()
}
class UserController(scope: InjectionScope) {
    private val service: UserService by scope()
}

class MessageRepository
class MessageService(scope: InjectionScope) {
    private val repository: MessageRepository by scope()
}
class MessageController(scope: InjectionScope) {
    private val service: MessageService by scope()
}

val userModule = tegralDiModule {
    put(::UserRepository)
    put(::UserService)
    put(::UserController)
}

val messageModule = tegralDiModule {
    put(::MessageRepository)
    put(::MessageService)
    put(::MessageController)
}

val environment = tegralDi {
    put(userModule)
    put(messageModule)
}
```

Modules are created using `tegralDiModule` followed by a module builder block, in which you can `put` components like you would in a regular component. However, you cannot affect the meta-environment from within a module, meaning that you cannot use a `meta` block, nor can you install extensions (e.g. `useServices` from the [services extension](./extensions/services.md)).

:::tip

The reason for this limitation is that modules are not necessarily always injected into extensible environments (i.e. modules may be injected in environments that do not have a meta-environment).

:::
