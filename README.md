# Python `.var` Postfix

A PyCharm plugin that adds IDEA-style `.var` + `Tab` completion for Python.

```python
get_user().var
```

Press `Tab` after `.var` to invoke PyCharm's native **Introduce Variable** refactoring. PyCharm manages variable naming, scope, occurrences, and undo/redo. Ambiguous expressions use the platform's expression chooser.

The plugin does not intercept typing or replace `Ctrl+Alt+V`. Postfix completion must be enabled in **Settings > Editor > General > Postfix Completion**. If you have changed its expansion shortcut, that shortcut applies to `.var` as well.

## Install

1. Download [pycharm-var-postfix-0.1.0.zip](releases/pycharm-var-postfix-0.1.0.zip) from this repository. Open the file page and use its download button; no source checkout or local build is required.
2. In PyCharm, open **Settings > Plugins > gear menu > Install Plugin from Disk**.
3. Select the ZIP without extracting it. Restart if requested.
4. Open a Python file, enter `get_user().var`, and press `Tab`.

## Compatibility

- Declared range: PyCharm 2023.3 through 2025.3 (`233` through `253.*`).
- Compilation and automated integration tests: PyCharm Community 2023.3.7, Java 17.
- Binary compatibility check: PyCharm 2025.3.6.1 (Build 253.33813.50).
- Intermediate versions and full interactive behavior have not been fully verified.
- No claim of compatibility with 2026.x or future releases until verified.

## Development

Requires JDK 17 and network access on the first build. The Gradle Wrapper is included; a global Gradle installation is unnecessary.

```powershell
.\gradlew.bat test buildPlugin verifyPluginStructure
.\gradlew.bat verifyPlugin
.\gradlew.bat runIde
```

The sandbox IDE is separate from your regular PyCharm settings and installed plugins.

To build a distributable ZIP and copy it into the repository's `releases/` directory:

```powershell
.\gradlew.bat prepareRelease
```

Commit the ZIP when publishing a release. Build caches and other generated files remain ignored.

## License

Licensed under the [MIT License](LICENSE). Copyright (c) 2026 Losino.
