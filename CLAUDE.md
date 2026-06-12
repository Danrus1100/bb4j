# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

bb4j is a Java library for reading, writing, migrating, and inspecting Blockbench
`.bbmodel` project files. It is published as a Maven artifact
(`com.danrus:bb4j`) — there is no application entry point or `main`, the public
surface is the API package.

## Build & test commands

Uses the Gradle wrapper. On Windows use `gradlew.bat`; the examples below use the
cross-platform form.

```bash
./gradlew build              # compile + assemble jar (also runs tests)
./gradlew test               # run JUnit 5 tests
./gradlew test --tests "com.danrus.bb4j.RoundTripTest.meshGeometrySurvivesRoundTrip"  # single test/method
./gradlew javadoc            # generate API docs under build/docs/javadoc
./gradlew publishToMavenLocal      # install to ~/.m2 for local consumers
./gradlew publish            # publish to the Shlakoblock Maven repo (needs creds)
```

Tests live in `src/test/java`; real `.bbmodel` fixtures are bundled under
`src/test/resources/models`. `RoundTripTest` enforces stable, lossless
read→write→read round-tripping (the central correctness property — see below).

Publishing to the remote repo requires `shlakoblock-maven-username` /
`shlakoblock-maven-password` Gradle properties. The build targets Java 17; the
only runtime dependency is Gson.

## Architecture

The flow is **bytes → typed document → (migration) → typed document → bytes**,
with everything orbiting `model.BbModelDocument`.

### Entry point
`api.BbModel` is the only class callers should need. It's a static facade with
overloaded `read(...)` / `write(...)` for `String`, `File`, `Path`,
`InputStream`/`OutputStream`, `Reader`/`Writer`, each taking an optional
`ReadOptions` / `WriteOptions` builder. It delegates to `io.BbModelReader` /
`io.BbModelWriter`.

### The document model (`model/`)
`BbModelDocument` is the in-memory representation of a whole `.bbmodel`. Top-level
collections (textures, elements, groups, outliner, animations, etc.) are typed
lists; smaller structures (Group, AnimationController, ReferenceImage, Collection,
ExportOptions, Warning) are nested static classes. Sub-packages:
- `geometry/` — `Element` (abstract) with `CubeElement` and `MeshElement`
  subtypes, plus `Face`, `Uv`. The reader picks the subtype from the `type` field.
- `animation/` — `Animation` → `Animator` → `Keyframe` → `DataPoint`.
- `outliner/` — the scene tree: `OutlinerNode` is either an
  `OutlinerGroupNode` (has children) or an `OutlinerElementRefNode` (a UUID string
  reference to an element). Parsing is recursive.
- `meta/`, `project/`, `texture/`.

### IO layer (`io/`)
`BbModelReader`/`BbModelWriter` do **manual, field-by-field Gson `JsonObject`
traversal** — there is no reflective `@SerializedName` mapping. When you add a
field to a model class you must wire it up in *both* the reader and the writer by
hand. `JsonCodec` wraps the configured Gson instance. `.bbmodel` files may be
LZ-UTF8 compressed; `BbModelFormatDetector` + `LzUtf8Codec` handle
detection/decompression, controlled by `CompressionMode` (AUTO/JSON/LZUTF8).

### Lossless round-tripping
A core design goal is preserving unknown fields so a read→write round-trip
doesn't lose data. With `ReadOptions.preserveExtraFields(true)` (the default),
unrecognized top-level keys are captured into `BbModelDocument.rawData` and
re-emitted on write. Each parse method in the reader captures its leftover keys
into the model object's `extra` `Map<String,Object>` via the
`extractExtra(json, knownKeys…)` helper, and every serialize method writes them
back via `writeExtra(json, obj.getExtra())`. **Both sides must stay symmetric** —
`RoundTripTest` asserts a second read→write reproduces the first byte-for-byte,
and `SemanticRoundTripTest` additionally asserts every value in the *original*
file survives the first write (catching losses the idempotence check misses).
Ambiguously-typed Blockbench fields (often "number-or-molang-string") are held
verbatim as `String` (e.g. `DataPoint` axes, `Animation.animTimeUpdate`); other
string-typed identifiers that look numeric (`Texture.id`, `Face.cullface`) are
kept as `String` rather than parsed, so their on-disk form is preserved exactly.

### Migration (`migrate/`)
`Migrator` runs an ordered list of `MigrationStep`s (`HeaderMigration`,
`CompatibilityMigration`, `OutlinerPre32Migration`, `AnimationPre50Migration`) —
each declares `shouldMigrate(doc)` and `migrate(doc)`. Triggered automatically
during `read` unless `VersionPolicy.IGNORE`. `VersionPolicy`
(STRICT/WARN/IGNORE, default WARN) decides what happens on an unsupported version:
throw, add a `BbModelDocument.Warning`, or silently proceed. Supported version
range lives in `SupportedVersions`; `FormatVersion` is a comparable
`major.minor[-beta.x]` value object.

### Molang (`molang/`)
Blockbench animation values are Molang expressions. `MolangInverter` negates an
expression as a string (used when mirroring/flipping animations);
`MolangEvaluator`/`MolangUtils` evaluate/manipulate them.

### Assets (`assets/`)
Pluggable resolution of texture sources behind the `AssetResolver` interface
(`FileSystemAssetResolver` for disk, plus `DataUrl`/`DataUrlDecoder` for inline
`data:` URLs). `TextureContentLoader` loads bytes; `PathPolicy` governs path
handling.

### High-level helpers (`api/utils/`)
`ModelInfo.fromDocument(doc)` is a read-oriented aggregator that composes
`ElementUtils`, `AnimationUtils`, `TextureUtils`, `OutlinerUtils` for convenient
queries. `ElementUtils`/etc. also expose `forDocument(doc)` factories.

## Conventions

- Errors thrown to callers are `api.BbException` (with factory helpers like
  `BbException.ioError(...)` and string error codes such as `"UNSUPPORTED_VERSION"`).
- Options classes use a fluent builder accessed via a static `builder()` that
  returns the instance itself (e.g. `ReadOptions.builder().versionPolicy(...)`).
- Coordinate/vector fields are `Double[]` / `Integer[]` (boxed, nullable) so that
  "absent" is distinguishable from "zero" during round-trips.
- Some doc comments and design notes are written in Russian.
