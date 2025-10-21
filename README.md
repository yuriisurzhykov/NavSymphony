# NavSymphony

**NavSymphony** is a highly flexible, declarative navigation framework for **Jetpack Compose** applications. It was designed for complex, stateful, policy-driven applications such as OEM, kiosk-mode, or enterprise systems, where navigation must be validated, restricted, or adapted dynamically at runtime.

---

## 🚀 Overview

NavSymphony extends the traditional idea of Jetpack Navigation by introducing **intent-driven navigation**, **requirements validation**, and **menu-aware graph composition**.

Instead of pushing destinations directly, navigation occurs through structured **intents**. Each intent passes through a chain of **validators** that can authorize, redirect, or ignore the request based on runtime conditions.

The result: predictable, secure, and extensible navigation — controlled from a single source of truth.

---

## 🧩 Key Features

* **Declarative Graph DSL** — Define screens, menus, and non-menu screens with appearance and timeout policies.
* **Requirements System** — Define per-screen access requirements (authentication, permissions, etc.) validated before navigation.
* **Intent Validation Chain** — Chain multiple validators with deterministic priority to intercept or redirect intents.
* **Timeouts & Policies** — Add per-screen or inherited timeouts with automatic logout or redirect.
* **Overlay Navigation** — Unified management for dialogs, progress windows, and modal overlays.
* **Composable ViewModel Navigation** — ViewModels initiate navigation via typed intents, keeping UI decoupled from navigation logic.
* **Safe Graph Evolution** — Built-in validation prevents duplicate routes, missing validators, or invalid configurations.

---

## 🏗️ Example Usage

### 1. Define Navigation Graph

```kotlin
val appGraph = graph {
    screen<HomeScreenRoute> {
        appearance = NodeAppearance.Primary
        timeout = 10.minutes
        require(AuthRequirement())
    }

    menu<SettingsMenuRoute> {
        items {
            screen<NetworkSettingsRoute> {
                require(ProtectedAreaRequirement())
            }
            screen<AboutAppRoute>()
        }
    }
}
```

### 2. Add Custom Requirement

```kotlin
class AuthRequirement : NavigationRequirement {
    override val key = "auth:loggedIn"
}

class AuthValidator : IntentValidationChain, PrioritizedValidator {
    override val priority = 0

    override suspend fun validate(intent: NavigationIntent, node: NavigationNode): ValidationResult {
        return if (SessionManager.isLoggedIn()) ValidationResult.Valid
        else ValidationResult.Redirect(AppNavigationIntent.NavigateTo(LoginScreenRoute()))
    }
}
```

### 3. Trigger Navigation from ViewModel

```kotlin
class LoginViewModel : NavigationViewModel<LoginScreenState, LoginIntent>(...) {

    override fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.Submit -> {
                if (authRepository.login(intent.credentials)) {
                    launchNavigation(AppNavigationIntent.NavigateTo(HomeScreenRoute()))
                } else {
                    displayOverlay(LoginFailedDialog())
                }
            }
        }
    }
}
```

### 4. Display a Progress Dialog

```kotlin
val dialog = ProgressDialogWindow(
    messageResolver = StringResolver.from(R.string.message_connecting),
    dismissEnabled = false
)

viewModel.displayOverlay(dialog)
```

---

## ⚙️ Architecture

### Components

| Component                   | Description                                                                                  |
| --------------------------- | -------------------------------------------------------------------------------------------- |
| **NavigationGraph**         | Root structure that defines all available screens and menus.                                 |
| **NavigationNode**          | Represents a single screen or menu item, including metadata and policies.                    |
| **NavigationChoreographer** | Core engine responsible for handling navigation intents and producing commands.              |
| **IntentValidationChain**   | Set of interceptors that validate, modify, or redirect navigation.                           |
| **NavigationRequirement**   | Declarative condition that a node must satisfy before being opened.                          |
| **NavigationViewModel**     | Base ViewModel with built-in navigation helpers (e.g. `launchNavigation`, `displayOverlay`). |
| **OverlayManager**          | Manages modal windows (dialogs, progress, confirmation, etc.) as part of navigation.         |

---

## 🧠 Validation System

Every navigation action (intent) passes through the **IntentValidationChain**, which checks requirements and contextual policies.

### Validation Flow

1. ViewModel emits `AppNavigationIntent`.
2. Intent is passed to `NavigationChoreographer`.
3. Choreographer runs all validators (sorted by priority).
4. Result is one of:

   * `ValidationResult.Valid` → continue navigation.
   * `ValidationResult.Redirect` → reroute to another screen.
   * `ValidationResult.Ignore` → cancel navigation silently.
   * `ValidationResult.Invalid` → stop navigation and show error.

---

## 🛠️ Dialog & Overlay Handling

Dialogs and progress windows extend `AbstractOverlayWindow`. They are:

* **Composable**, reusable, and styleable.
* **Lifecycle-safe** — dismissed automatically when host screen exits.
* **Serializable** (if implementing `OverlayMemento`) for process restoration.

Example:

```kotlin
@Immutable
class NetworkErrorDialog(
    private val message: StringResolver,
    override val onDismiss: () -> Unit = {}
) : AbstractDialogWindow(onDismiss) {
    @Composable
    override fun rememberDialogButtons(): ImmutableList<DialogButton> =
        persistentListOf(DialogButton.ok(textResolver = message))
}
```

---

## 🧪 Testing

Use provided testing utilities to build and validate navigation graphs in isolation.

```kotlin
runNavigationTest {
    val graph = buildGraph()
    val choreographer = NavigationChoreographer(graph)

    sendIntent(AppNavigationIntent.NavigateTo(HomeScreenRoute()))

    assertCommands(
        NavigationCommand.OpenScreen(HomeScreenRoute())
    )
}
```

---

## 🪶 Roadmap

| Milestone                   | Description                                                |
| --------------------------- | ---------------------------------------------------------- |
| **M1 – Stability**          | Deterministic validators, safe overlays, graph validation. |
| **M2 – UX & Observability** | Telemetry, global timeout manager, deeplink integration.   |
| **M3 – DevX & Safety**      | KSP compile-time validation, Detekt rules, test helpers.   |

---

## 📦 Installation

*Coming soon.* Once the library stabilizes, it will be published to Maven Central.

For now, clone and include as a Gradle module:

```kotlin
implementation(project(":navsymphony"))
```

---

## 🧭 Philosophy

> *"Navigation isn’t just about screens — it’s about control, context, and correctness."*

NavSymphony is built to scale from simple Compose apps to multi-module embedded systems.
It bridges declarative navigation and enterprise-grade control — where **every screen transition is intentional, validated, and safe**.

---

## 🧑‍💻 Author

**Yurii Surzhykov** — Android & System Developer,
Building composable architectures for real-world reliability.

📍 Vancouver, WA, USA

---

## 📝 License

```
Copyright (c) 2025 Yurii Surzhykov

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
