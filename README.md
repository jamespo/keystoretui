# Keystore TUI Viewer

A simple Terminal User Interface (TUI) application for viewing Java Keystore (JKS) and PKCS12 files. Built with Java and the [Lanterna](https://github.com/mabe02/lanterna) library.

## Features

- Browse aliases in a keystore.
- View certificate details (Subject, Issuer, Validity, etc.).
- Support for JKS and PKCS12 formats.
- Interactive TUI with password prompting.

## Prerequisites

- Java 21 or higher.

## Building the Project

The project uses Gradle. You can build a runnable "fat" JAR using the provided Gradle wrapper.

### On Linux/macOS:
```bash
./gradlew shadowJar
```

### On Windows:
```cmd
gradlew.bat shadowJar
```

This will create `keystoretui.jar` in the root directory of the project.

## Usage

You can run the application by passing the path to a keystore file as an argument:

```bash
java -jar keystoretui.jar [path-to-keystore]
```

If no path is provided, it defaults to looking for `java-cacerts.jks` in the current directory.

### Example:
```bash
java -jar keystoretui.jar my-keystore.p12
```

Once started, the application will:
1. Prompt for the keystore password.
2. Display a list of aliases found in the keystore.
3. Allow you to select an alias to view its certificate details.

## Development

- **Main Class:** `keystoretui.App`
- **TUI Library:** Lanterna
- **Build Tool:** Gradle
