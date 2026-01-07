# How to Contribute

We’re really happy to have you here! Here are some guidelines to help you get started.

## Development

When something is changed in the library project, you need to publish the artifact locally for the 
changes to take effect in the sample projects. 

After modifying the library source code, run the following command:

```Bash
./gradlew publishToMavenLocal -Pskip.signing
```

After this, sync the sample project so it reflects the latest changes.

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
./gradlew updateLegacyAbi
```

### Tests

Ensure that all tests are passing by running the commands below:

```Bash
# Android tests (make sure an emulator or device is running)
./gradlew connectedAndroidTest

# iOS tests (requires macOS)
./gradlew iosSimulatorArm64Test
```