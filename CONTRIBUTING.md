# How to Contribute

We’re really happy to have you here! Here are some guidelines to help you get started.

## Pull Requests

Before opening a pull request, make sure all contribution guidelines are followed.

### Spotless

Run spotless and resolve any formatting or dependency issues if needed.

```Bash
./gradlew spotlessApply
```

### ABI (Binary Compatibility)

To ensure your changes do not break binary compatibility in future releases, run the following command:

> If you are not familiar with ABI, please refer to the official documentation

```Bash
./gradlew apiDump
```

### Tests

Ensure that all tests are passing by running the commands below:

```Bash
# Android tests (make sure an emulator or device is running)
./gradlew connectedAndroidTest

# iOS tests (requires macOS)
./gradlew iosSimulatorArm64Test
```