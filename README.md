# IkogeMind

Native Android chat app powered by free-tier AI models (Gemini free tier +
OpenRouter free models), routed through a swappable pipeline. See the project's
`architecture.md`, `model-routing.md`, `screens-and-flows.md`, `brand-notes.md`,
and `decisions-log.md` for the full context this build follows.

## Status

v1 skeleton — every screen, ViewModel, DAO, pipeline step, and network client
described in `architecture.md` exists as real Kotlin, wired end to end, and
compiles clean via CI:

```
ChatScreen → ChatViewModel → PipelineOrchestrator
  → ContextStep (Room) → ModelStep (ModelRouter) → PostProcessStep → FormatOutputStep
  → back to Room → Flow back up to ChatScreen
```

## To open this project

1. Open the `IkogeMind/` folder in Android Studio (Koala+ recommended).
2. Let Gradle sync — it will pull dependencies from `app/build.gradle.kts`.
3. In Settings screen (once running), paste a Gemini AI Studio API key and/or
   any of the three OpenRouter keys (Llama 3.1 405B / Qwen3 Coder / gpt-oss-120b).
   Nothing works without at least one.
4. Run on a device/emulator with API 26+.

The Gradle wrapper scripts/jar aren't committed to this repo (pushed via the
GitHub API from a sandbox that can't safely transmit binary file content, and
the wrapper jar is binary). Two ways to get a working `./gradlew` locally:
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

- **Non-streaming model calls.** Both `GeminiApi` and `OpenRouterApi` call
  non-streaming endpoints and return the full response at once. The "Streaming"
  chat state currently just means "waiting on the pipeline." True token-by-token
  streaming is a real upgrade, isolated to `ModelStep` + the Gemini/OpenRouter
  clients — nothing else needs to change.
- **No app icon / brand colors** — placeholder vector mark; the in-app palette
  (background/accent/text tokens) is set per `brand-notes.md`, but no launcher
  icon exists yet.
- **Conversation rename/delete-from-list UI** isn't built (delete method exists in
  `ChatRepository`/`ConversationListViewModel`, no UI trigger yet) — matches
  `screens-and-flows.md`'s "not decided yet."

Resolved since the list above was first written: API keys are now encrypted at
rest via Android Keystore AES-GCM (`KeystoreCrypto.kt`), not plaintext; the
OpenRouter fallback is 3 hand-picked, individually-keyed free models
(Llama 3.1 405B → Qwen3 Coder → gpt-oss-120b) with an auto-router safety net,
not a guessed single slug; and the dead "preferred provider" setting (stored
but never read by `ModelRouter`) was removed rather than left as confusing
unused state.

## Why no Hilt / DI framework

One dev, a handful of ViewModels — `IkogeMindApp` is a manual service locator and
`ViewModelFactories.kt` hand-wires each ViewModel. Revisit only if the object graph
gets genuinely tangled; swapping in Hilt later is a localized change (those two
files), not a rewrite.
