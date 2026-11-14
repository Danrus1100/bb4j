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
./gradlew publish            # publish to the Github Maven repo (needs creds)
```

Tests live in `src/test/java`; real `.bbmodel` fixtures are bundled under
`src/test/resources/models`. `RoundTripTest` enforces stable, lossless
read→write→read round-tripping (the central correctness property — see below).

## Architecture

You can fide architecture structure in `ARCHITECTURE.md`
