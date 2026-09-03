# IkogeMind

Native Android chat app powered by free-tier AI models (Gemini free tier +
OpenRouter free models), routed through a swappable pipeline. See the project's
`architecture.md`, `model-routing.md`, `screens-and-flows.md`, `brand-notes.md`,
and `decisions-log.md` for the full context this build follows.

## Status

v1 skeleton — compiles-in-spirit, not yet opened in Android Studio / run. Every
screen, ViewModel, DAO, pipeline step, and network client described in
`architecture.md` exists as real Kotlin, wired end to end:

```
ChatScreen → ChatViewModel → PipelineOrchestrator
  → ContextStep (Room) → ModelStep (ModelRouter) → PostProcessStep → FormatOutputStep
  → back to Room → Flow back up to ChatScreen
```

## To open this project

1. Open the `IkogeMind/` folder in Android Studio (Koala+ recommended).
2. Let Gradle sync — it will pull dependencies from `app/build.gradle.kts`.
3. In Settings screen (once running), paste a Gemini AI Studio API key and/or an
   OpenRouter API key. Nothing works without at least one.
4. Run on a device/emulator with API 26+.

The Gradle wrapper scripts/jar aren't committed to this repo (this project was
pushed via the GitHub API from a sandbox that can't safely transmit binary file
content, and the wrapper jar is binary). Two ways to get a working `./gradlew`
locally:
- Open the project in Android Studio — it offers to generate the wrapper
  automatically.
- Or run `gradle wrapper --gradle-version 8.7` once if you have Gradle installed.

## CI

`.github/workflows/android-build.yml` builds a debug APK on every push/PR to
`main` using GitHub-hosted runners (which have a JDK + Android SDK available,
unlike the sandbox this project was scaffolded in). CI provisions Gradle
directly (`gradle/actions/setup-gradle`) rather than depending on the missing
wrapper jar. Grab the APK from the workflow run's Artifacts tab.

## Known gaps / next decisions (flagged, not blocking)

- **API key storage is plaintext DataStore**, not encrypted. Fine for solo personal
  testing; swap for `EncryptedSharedPreferences` before this reaches anyone else.
  See `SettingsRepository.kt`.
- **Non-streaming model calls.** Both `GeminiApi` and `OpenRouterApi` call
  non-streaming endpoints and return the full response at once. The "Streaming"
  chat state currently just means "waiting on the pipeline." True token-by-token
  streaming is a real upgrade, isolated to `ModelStep` + the Gemini/OpenRouter
  clients — nothing else needs to change.
- **OpenRouter free model list is a guess** (`OpenRouterApi.FREE_MODEL_FALLBACK_ORDER`).
  Confirm current free-tier slugs before relying on it — this was flagged as a TODO
  in `model-routing.md` and wasn't resolved here.
- **Preferred-provider setting is stored but not yet read** by `ModelRouter` — it
  always does Gemini-first/OpenRouter-fallback regardless of the Settings screen
  selection. Wiring it in is a small, contained change to `ModelRouter.sendMessage`.
- **No app icon / brand colors** — placeholder vector mark and a neutral dark
  palette, per `brand-notes.md`'s own "to fill in" list.
- **Conversation rename/delete-from-list UI** isn't built (delete method exists in
  `ChatRepository`/`ConversationListViewModel`, no UI trigger yet) — matches
  `screens-and-flows.md`'s "not decided yet."

## Why no Hilt / DI framework

One dev, a handful of ViewModels — `IkogeMindApp` is a manual service locator and
`ViewModelFactories.kt` hand-wires each ViewModel. Revisit only if the object graph
gets genuinely tangled; swapping in Hilt later is a localized change (those two
files), not a rewrite.
