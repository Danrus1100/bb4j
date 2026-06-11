# bb4j — Blockbench model library for Java

A small, dependency-light Java library for reading, writing, migrating and
inspecting Blockbench [`.bbmodel`](https://www.blockbench.net/) project files.

It parses a `.bbmodel` into a strongly-typed document model, lets you query and
modify it, and writes it back — with a strong focus on **lossless round-tripping**:
unknown and future fields are preserved, so reading a file and writing it back
does not throw your data away.

## Features

- **Read & write** `.bbmodel` files from a `String`, `File`, `Path`,
  `InputStream`/`OutputStream` or `Reader`/`Writer`.
- **Typed document model** — textures, elements (cubes & meshes), faces, groups,
  the outliner scene tree, animations/keyframes, display slots, reference images
  and more.
- **Lossless round-trip** — unrecognized and ambiguously-typed fields are kept
  verbatim and re-emitted, so consuming a newer Blockbench file never silently
  drops data.
- **Transparent LZ-UTF8 compression** — Blockbench's compressed `.bbmodel`
  variant is auto-detected on read and can be produced on write.
- **Version migration** — older files (format `3.2`–`4.x`) are upgraded on read,
  with a configurable policy for unsupported versions.
- **High-level helpers** — `ModelInfo` and friends for quick inspection
  (counts, bounding size, animation duration, …) and Molang expression
  evaluation/inversion.

## Requirements

- Java 17+
- Single runtime dependency: [Gson](https://github.com/google/gson)

## Installation

The artifact is published as `com.danrus:bb4j`.

**Gradle (Kotlin DSL):**

```kotlin
repositories {
    mavenCentral()
    maven("https://maven.shlakoblock.com/releases")
}

dependencies {
    implementation("com.danrus:bb4j:1.2")
}
```

**Maven:**

```xml
<repositories>
    <repository>
        <id>shlakoblock</id>
        <url>https://maven.shlakoblock.com/releases</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.danrus</groupId>
    <artifactId>bb4j</artifactId>
    <version>1.2</version>
</dependency>
```

## Quick start

Everything goes through the `BbModel` facade.

```java
import com.danrus.bb4j.api.BbModel;
import com.danrus.bb4j.model.BbModelDocument;

import java.nio.file.Path;

// Read (LZ-UTF8 compression and version migration handled automatically)
BbModelDocument doc = BbModel.read(Path.of("model.bbmodel"));

// Inspect
System.out.println("Elements:   " + doc.getElements().size());
System.out.println("Textures:   " + doc.getTextures().size());
System.out.println("Animations: " + doc.getAnimations().size());

// Modify
doc.getMeta().setName("my-renamed-model");

// Write back (pretty-printed JSON by default)
BbModel.write(doc, Path.of("out.bbmodel"));
```

### Inspecting a model

The `ModelInfo` helper aggregates common read-only queries:

```java
import com.danrus.bb4j.api.utils.ModelInfo;

ModelInfo info = ModelInfo.fromDocument(doc);

info.getModelFormat();        // e.g. "java_block", "bedrock", "free"
info.getFormatVersion();      // e.g. "5.0"
info.getCubeCount();
info.getMeshCount();
info.getTextureCount();
info.getAnimationCount();
info.getModelWidth();         // bounding-box dimensions
info.getAnimationDuration();  // length of the longest animation, in seconds

System.out.println(info);     // human-readable summary
```

## Reading options

Pass a `ReadOptions` to customize reading:

```java
import com.danrus.bb4j.api.ReadOptions;
import com.danrus.bb4j.api.VersionPolicy;
import com.danrus.bb4j.api.CompressionMode;

BbModelDocument doc = BbModel.read(json, ReadOptions.builder()
        .versionPolicy(VersionPolicy.WARN)        // STRICT | WARN | IGNORE (default WARN)
        .compressionMode(CompressionMode.AUTO)    // AUTO | JSON | LZUTF8 (default AUTO)
        .preserveExtraFields(true));              // keep unknown fields (default true)
```

`VersionPolicy` controls what happens when a file's `format_version` is outside
the supported range:

| Policy   | Behaviour on an unsupported version                                   |
|----------|-----------------------------------------------------------------------|
| `STRICT` | throws `BbException` with error code `UNSUPPORTED_VERSION`             |
| `WARN`   | proceeds and records a `BbModelDocument.Warning` (default)             |
| `IGNORE` | proceeds silently and **skips migration** (the file is read as-is)    |

> Note: with `IGNORE`, migrations are not applied and the original
> `format_version` is preserved on write. With `WARN`/`STRICT` a migrated
> document is stamped with the latest format version on write.

## Writing options

```java
import com.danrus.bb4j.api.WriteOptions;
import com.danrus.bb4j.api.CompressionMode;

String out = BbModel.write(doc, WriteOptions.builder()
        .prettyPrint(true)                        // pretty JSON (default) vs compact
        .compressionMode(CompressionMode.LZUTF8)  // write Blockbench's compressed format
        .includeEditorState(true)                 // include editor_state (default false)
        .includeHistory(true));                   // include undo history (default false)
```

Editor state and undo history are excluded by default, since they are usually
not wanted by downstream consumers.

## Supported formats

| Blockbench `format_version` | Support                                  |
|-----------------------------|------------------------------------------|
| `5.0`                       | native                                   |
| `3.2` – `4.x`               | read & migrated up to the latest format  |
| `< 3.2`                     | unsupported (handled per `VersionPolicy`)|

All Blockbench model formats (`java_block`, `bedrock`, `free`/generic, `skin`,
…) are read and written through the same document model.

## Lossless round-tripping

A core design goal is that a `read → write` round-trip does not lose data.
Unrecognized top-level keys are captured into `BbModelDocument.getRawData()`,
and unknown nested keys into each object's `extra` map, then re-emitted on write.
Ambiguously-typed Blockbench fields (e.g. number-or-Molang values, string
identifiers that happen to look numeric) are kept verbatim so their on-disk form
is preserved exactly.

## Error handling

All failures surface as `com.danrus.bb4j.api.BbException` (an unchecked
exception) carrying a short, stable error code such as `PARSE_ERROR`,
`IO_ERROR`, or `UNSUPPORTED_VERSION`.

## Building from source

```bash
./gradlew build              # compile + test + assemble the jar
./gradlew test               # run the JUnit 5 test suite
./gradlew publishToMavenLocal # install into ~/.m2 for local use
```

## License

See [License.md](License.md).
