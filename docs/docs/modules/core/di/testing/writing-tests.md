---
sidebar_position: 1
---

# Writing tests

Tegral DI provides many patterns that can be used for writing tests.

## Subject-based testing

Tegral DI provides a class that can be used by your test classes to automatically create very flexible test environments. Let's imagine we want to test our `UserRegistrationService` class, as follows:

```kotlin
class RegistrationException(message: String) : Exception(message)

class UserRegistrationService(scope: InjectionScope) {
    private val userRepository: UserRepository by scope()
    private val permissionsService: PermissionsService by scope()

    fun registerUser(registration: UserRegistrationData) {
        when {
            !registration.isRegistrationValid() ->
                throw RegistrationException("Registration is invalid")
            !permissionsService.isUserAllowedToRegister(registration) ->
                throw RegistrationException("User is not allowed to register")
            else ->
                userRepository.createFrom(registration)
        }
    }
}
```

Let's say we want to test the `registerUser` function. We can create a test class like this:

```kotlin
class UserRegistrationServiceTest : TegralSubjectTest<UserRegistrationService>(
    UserRegistrationService::class,
    tegralDiModule {
        put { UserRegistrationService() }
    }
) {
    // ...
}
```

The `TegralSubjectTest` class is based around the idea of a "test subject" -- that is, some component that we are trying to test. In this case, this is the `UserRegistrationService` class. We need to provide:

- The `KClass` for this service.
- A base module that will be used to create the test subject and any additional objects you will always need in the environment within your tests. This one can be initialized in a few ways:
  - By directly providing a module (either by using one that you already made before *or* by creating a new one via `tegralDiModule`).

  - By providing a lambda that will create the module. This basically means passing the lambda you'd use with `tegralDiModule`, but without the `tegralDiModule` that goes with it.

  - By providing a constructor. This is useful if you only need the test subject within your test, and that subject's constructor is simple, similiar to using `put { ... }` versus `put(::...)`. The class is automatically extracted from the function's signature. Note that this approach may be more bug-prone and less flexible than the above ones.

```kotlin
// Using a lambda
class UserRegistrationServiceTest : TegralSubjectTest<UserRegistrationService>(
    UserRegistrationService::class, { put { UserRegistrationService() } }
) {
    // ...
}

// Using a constructor reference
class UserRegistrationServiceTest : TegralSubjectTest<UserRegistrationService>(
    ::UserRegistrationService
) {
    // ...
}
```

## The `test` function

Once you have set up your class, you can use the `test` function to automatically create an injection environment.

```kotlin
class UserRegistrationServiceTest : TegralSubjectTest<UserRegistrationService>(
    UserRegistrationService::class,
    tegralDiModule {
        put { UserRegistrationService() }
    }
) {
    @Test
    fun `Test fails if registration is invalid`() = test {
        val registration = mockk<UserRegistrationData> {
            every { isRegistrationValid() } returns false
        }
        put { registration }
        assertThrows<RegistrationException> {
            subject.registerUser(registration)
        }
        verify { registration.isRegistrationValid() }
    }
}
```

In this example, we're using [MockK](https://mockk.io) to create a mocked user registration data object where `isRegistrationValid` always returns false. Then, we add it to the injection environment (using `put`). We then call `registerUser` and assert that it throws a `RegistrationException`, as wrong user registrations should trigger this kind of exception. We then use MockK's `verify` function to ensure that the `isRegistrationValid` function was called.

You have access to all the usual environment methods (`get` and `createInjector`) and all DSL component creation methods (the `put` function family) within the `test` block. `subject` is a shortcut for `get`ting the test subject: in this example, it's equivalent to `get<UserRegistrationService>()`.

### Managing mocks

Since the pattern of "mock (or create something) and put it in the environment" is common, you can save a line by using `.alsoPut()` instead of a separate `put` call. This is equivalent to calling `.also { put(it) }`, but uses a nicer format.

```kotlin
class UserRegistrationServiceTest : TegralSubjectTest<UserRegistrationService>(
    UserRegistrationService::class,
    tegralDiModule {
        put { UserRegistrationService() }
    }
) {
    @Test
    fun `Registration fails if data is invalid`() = test {
        // highlight-start
        val registration = mockk<UserRegistrationData> {
            every { isRegistrationValid() } returns false
        }.alsoPut()
        // highlight-end
        assertThrows<RegistrationException> {
            subject.registerUser(registration)
        }
        verify { registration.isRegistrationValid() }
    }
}
```

And for an even easier time, you can use the `tegral-di-test-mockk` library that adds a `putMock` function that does all of this in one go:

```kotlin
class UserRegistrationServiceTest : TegralSubjectTest<UserRegistrationService>(
    UserRegistrationService::class,
    tegralDiModule {
        put { UserRegistrationService() }
    }
) {
    @Test
    fun `Registration fails if data is invalid`() = test {
        // highlight-start
        val registration = putMock<UserRegistrationData> {
            every { isRegistrationValid() } returns false
        }
        // highlight-end
        assertThrows<RegistrationException> {
            subject.registerUser(registration)
        }
        verify { registration.isRegistrationValid() }
    }
}
```

You can also create your own extension function that does all of this for you if you are using libraries other than MockK. For example, here is a simplified version of the implementation provided by `tegral-di-test-mockk`:

```kotlin
inline fun <reified T> UnsafeMutableEnvironment.putMockk(block: T.() -> Unit): T =
        mockk(block = block).alsoPut()
```

We can then continue on with this pattern and mock the behaviors we need for the three cases in our `registerUser` function.

```kotlin
@Test
fun `Registration fails if data is invalid`() = test {
    val registration = putMockk<UserRegistrationData> {
        every { isRegistrationValid() } returns false
    }
    assertThrows<RegistrationException> {
        subject.registerUser(registration)
    }
    verify { registration.isRegistrationValid() }
}

@Test
fun `Test fails if registration is invalid`() = test {
    val registration = putMockk<UserRegistrationData> {
        every { isRegistrationValid() } returns true
    }

    val perms = putMockk<PermissionsService> {
        every { isUserAllowedToRegister(registration) } returns false
    }

    assertThrows<RegistrationException> {
        subject.registerUser(registration)
    }
    verify {
        registration.isRegistrationValid()
        perms.isUserAllowedToRegister(registration)
    }
}

@Test
fun `Registration succeeds and calls repository`() = test {
    val registration = putMockk<UserRegistrationData> {
        every { isRegistrationValid() } returns true
    }
    val perms = putMockk<PermissionsService> {
        every { isUserAllowedToRegister(registration) } returns true
    }
    val repo = putMockk<UserRepository> {
        every { registerUser(registration) } just runs
    }
    assertDoesNotThrow {
        subject.registerUser(registration)
    }
    verify {
        registration.isRegistrationValid()
        perms.isUserAllowedToRegister(registration)
        repo.registerUser(registration)
    }
}
```

### Mock in module, specs in test

One possible pattern is to mock an object that is almost always used as a dependency in your test subject within the base module. Using MockK, you can then add more behavior to this mock on the fly, within tests where such behavior matters.

```kotlin
class UserRegistrationServiceTest : TegralSubjectTest<UserRegistrationService>(
    UserRegistrationService::class, {
        put { UserRegistrationService() }
        // highlight-start
        put { mockk<UserRegistrationData>() }
        // highlight-end
    }
) {
    @Test
    fun `Registration fails if data is invalid`() = test {
        // highlight-start
        val registration = get<UserRegistrationData>().apply {
            every { isRegistrationValid() } returns false
        }
        // highlight-end
        assertThrows<RegistrationException> {
            subject.registerUser(registration)
        }
        verify { registration.isRegistrationValid() }
    }
}
```

### Running tests in parallel

Tegral DI's environments are strongly scoped: that is, they live independently of each other without any global state. This means that, as long as your own classes do not rely on a kind of "global state", your tests can be ran in parallel without issues.

## Manually creating a test environment

While subject-based testing is handy, you may want to create an environment yourself without using this pattern. In this case, you can create environments as usual and use the `UnsafeMutableEnvironment` environment type.

```kotlin
val env = tegralDi(UnsafeMutableEnvironment) {
    put(::MyComponent)
    put(::MyOtherComponent)
}

env.put(...)
env.get<...>(...)
```

This is the same kind of environment you get from subject-based tests.
