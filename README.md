### Set up development environment

1. Clone the repository
2. Ensure you have a Java 17 JDK installed on your system and added to JAVA_PATH
3. Run `./gradlew` to download all dependencies and configure the project (can take 10-15 minutes)

### Compile and build project

- `./gradlew desktop:run` to run the desktop application
- `./gradlew desktop:packageReleaseMsi` to package the app for Windows 10+
- `./gradlew desktop:packageReleaseAppImage` to package the app as a portable AppImage for Linux
- `./gradlew desktop:packageReleaseDmg` to package a `.dmg` for macOS that is **not notarized**
- (_Not recommended_) `./gradlew desktop:packageReleaseRpm` to create an architecture-specific `.rpm` package for Linux
- (_Not recommended_) `./gradlew desktop:packageReleaseDeb` to create an architecture-specific `.deb` package for Linux

**Note**: to compile an AppImage, a `.deb` or an `.rpm` package, install `fakeroot` and `binutils` first. For `.rpm` packages specifically, you will also need `rpmbuild` installed.
