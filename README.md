# EveryStory

A calm Android news reader for reading past the headline. Built with Kotlin and Jetpack Compose on
[NewsAPI](https://newsapi.org).

**Try it:** [download the APK](https://github.com/Marcostar/newsPoc/releases/latest) (Android 8.0+) ·
[project page](https://marcostar.github.io/newsPoc/)

No red, no "BREAKING", no unread counters, no autoplay. Headlines, search, saved stories that open
offline, and a reader that treats text size and dark mode as first-class.

## What's in it

**The brief**

| Requirement | Where |
|---|---|
| Latest headlines (`/v2/top-headlines`) | Today tab, topic chips (Business, Health, Science…) |
| Keyword search (`/v2/everything`) | Search bar on Today and Saved |
| Manual refresh | Refresh link and pull to refresh |
| Detail: title, source, date, link out | Story screen, opens the article in a Custom Tab |
| Pagination | 20 stories a page; the next page starts loading 5 stories before the end |
| Graceful errors | Invalid key, offline, timeout, server error, rate limit (see below) |
| Firebase Analytics | One `AnalyticsTracker` interface, events listed below |
| Bonus: animations, favourites, dark mode | Shared headline/image transition, Saved tab, day/night palettes |

**Beyond the brief**

- Offline first: the feed and every opened story are cached in Room; saved stories open with no connection.
- Online/offline banner, and "Back online. Refresh for the latest stories."
- Text size (7 steps on top of the system font size) and Light / Dark / System theme.
- News in English, Deutsch, Français and العربية (Arabic stories render right to left).
- Read aloud with word and sentence highlighting.
- A first-launch tour, a reminder for stories saved and not yet read, a data usage screen.
- One test banner ad (AdMob, with Google's consent form) on the Saved tab.

## Build from source

Requirements: a recent Android Studio with AGP 9.4 support (it brings its own JDK), Android SDK 37.

1. Get a free key at [newsapi.org](https://newsapi.org/register).
2. Add it to `local.properties` in the project root (this file is git-ignored):
   ```properties
   NEWS_API_KEY=your_key_here
   ```
3. Run the `app` configuration, or:
   ```bash
   ./gradlew installDebug
   ```

`app/google-services.json` is committed on purpose: it holds Firebase project identifiers, not secrets,
so the project builds without extra setup.

**Release builds** are shrunk and obfuscated with R8. Signing is optional: add `RELEASE_STORE_FILE`,
`RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS` and `RELEASE_KEY_PASSWORD` to `local.properties`;
without them `./gradlew assembleRelease` produces an unsigned APK.

## Architecture

MVVM on Android's recommended app architecture, three layers with dependencies pointing inward:

```
ui (feature/*)      Route (stateful) → Screen (stateless) ⇄ ViewModel → StateFlow<UiState>
domain              models · repository interfaces · use cases (only where there is real logic)
data                Default/Room/DataStore repositories → Retrofit service + Room DAOs
```

- **Single activity, Compose only**, Navigation Compose with type-safe `@Serializable` destinations.
  Each bottom tab (Today, Saved, You) keeps its own back stack.
- **Manual dependency injection**: one `AppContainer` builds Retrofit, OkHttp, Room and DataStore
  once; each ViewModel has a `Factory` next to it. Swapping to Hilt would only touch `di/` and the
  factories.
- **Unidirectional data flow**: screens send events up (`onRefresh()`, `onQueryChange()`), state comes
  down as one immutable `UiState`. One-off results (snackbars) are state too, not channels.
- **Manual pagination** instead of Paging 3: the ViewModel holds `items`, `page` and
  `loadMoreState` (`Idle | Loading | Error | EndReached`). One request at a time, no automatic retry
  after an error, refresh keeps the old list on screen until the new one arrives. Paging 3 is the
  upgrade path if the feed grows.
- **Offline first**: page one of each feed and every loaded article go into Room (pruned after 7
  days). NewsAPI has no "get article by id", so the Story screen reads from that cache, which is also
  why it survives process death.
- **Search**: 300 ms debounce, 2 characters minimum, the same query is never sent twice, Enter
  searches at once. Unmatched brackets are dropped before sending (NewsAPI rejects them).
- **Coroutines and Flow** throughout; repositories are main-safe (`withContext(ioDispatcher)`, with the
  dispatcher injected for tests).

```
com.m10.everystory
├── core/        designsystem (theme, components, icons), network, database, analytics, ui helpers…
├── domain/      model, repository (interfaces), usecase
├── data/        news, saved, search, preferences, apikey, usage
├── feature/     today, search, story, saved, you, display, tour, developer, datausage, profile
├── navigation/  EsNavHost, destinations, top-level tabs
└── di/          AppContainer
```

## Errors

The data layer catches exceptions and maps them to a sealed `AppError`; the UI maps that to a
sentence and one action. No error codes on screen (they go to the log), and content already on
screen is never cleared by a failed refresh.

| Problem | What the reader sees |
|---|---|
| No connection | "You're offline", plus a banner on every screen pointing to Saved stories |
| Invalid key (401) | "We can't load stories right now" · "The news service didn't accept this app's key." |
| Free requests used up (429) | "Free news used up for now", with a "Use your own key" button |
| Timeout | "The news service is taking too long. Try again in a moment." |
| Server error (5xx) | "The news service isn't responding. This isn't a problem with your phone." |
| Past result 100 (426) | Treated as the end of the list, not an error |

## Analytics

Everything goes through `core/analytics/AnalyticsTracker` (Firebase implementation in the app, a fake
in tests), and every event is listed in `AnalyticsEvent`:

`screen_view`, `search`, `topic_select`, `feed_refresh`, `load_error`, `story_open`, `story_save`,
`story_unsave`, `story_link_out`, `listen_start`, `theme_change`, `text_size_change`,
`language_change`, `tour_complete`, `tour_skip`, `own_api_key`, `reminder_open`, `ad_impression`,
`banner_click`, `profile_open`.

Search terms are sent to Analytics but never written to the log. Collection is switched off on
Firebase Test Lab devices so automated runs don't count as readers.

## Testing

```bash
./gradlew testDebugUnitTest
```

- **160 unit tests**: every ViewModel (Turbine + `runTest`), use cases, mappers, formatters, and the
  repository against `MockWebServer` (401, 429, 5xx, 426, timeout, offline). Hand-written fakes in
  `testing/`, no mocking library.
- **Monkey stress test** (`MonkeyStressTest`, instrumentation): runs Android's monkey against the app and
  fails on a crash, ANR or early stop. Runs locally or on Firebase Test Lab:
  ```bash
  ./gradlew assembleDebug assembleDebugAndroidTest
  gcloud firebase test android run --type instrumentation \
    --app app/build/outputs/apk/debug/app-debug.apk \
    --test app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
    --device model=MediumPhone.arm,version=26 --device model=MediumPhone_ps16k.arm,version=37
  ```
  Last run: 3,000 events each on Android 8.0 and Android 17, plus a Robo crawl on both. No crashes,
  ANRs or leaks.
- **StrictMode** (debug builds) logs main-thread disk and network access; **LeakCanary** (debug only)
  watches for leaks.
- The release build was smoke-tested on a Pixel 10 Pro XL (feed, search, story, link out, save,
  read aloud, rotation, process death, errors).

## Things worth knowing

- **Free NewsAPI plan.** 50 requests every 12 hours, shared by everyone using the same key. When they
  run out the app says so and offers to use your own key.
- **Hidden developer screen.** Tap the EveryStory name on Today three times (or You › Help › Use your
  own NewsAPI key) to enter your own key. It's checked with NewsAPI before saving and stored
  encrypted with the Android Keystore.
- **And one more.** Tap the version row under You three times.
- **The key is in the APK.** The published APK carries a NewsAPI key in its code, which anyone can
  extract. Fine for a demo; a real release would put a small backend proxy in front of NewsAPI so no
  key ships in the app (plus App Check and per-user rate limits).
- **Other languages.** On the free plan `top-headlines` only returns English by language, so German,
  French and Arabic headlines come from each language's outlets instead.
- **Ads** use Google's sample IDs. Debug builds pretend to be in the EEA so the consent form always
  shows.

## Tech stack

Kotlin 2.2 · Jetpack Compose (Material 3, custom theme) · Navigation Compose · Lifecycle ViewModel ·
Coroutines + Flow · Retrofit 3 + OkHttp + kotlinx.serialization · Room · DataStore · Coil 3 ·
WorkManager · Firebase Analytics · AdMob + UMP · Custom Tabs · Timber · LeakCanary · JUnit, Turbine,
MockWebServer · minSdk 26, targetSdk 37
