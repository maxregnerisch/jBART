# MaxRegner OneUI Porter v7.0

A modern Java-based tool for porting between Samsung OneUI 7 ROMs on Android 15.

## Features

- Extract and analyze OneUI 7 ROMs (OTA packages and Samsung firmware)
- Handle modern Android partition structures (dynamic partitions, super partitions)
- Port system apps, vendor blobs, and product configurations between ROMs
- Preserve device-specific drivers and configurations
- User-friendly GUI interface for easy operation
- Detailed logging for troubleshooting

## Requirements

- Java 11 or higher
- At least 16GB of RAM recommended
- At least 100GB of free disk space
- Windows, macOS, or Linux operating system

## Building from Source

### Prerequisites

- Java JDK 11 or higher
- Maven

### Build Instructions

1. Clone the repository:
   ```
   git clone https://github.com/maxregnerisch/oneui-porter.git
   cd oneui-porter
   ```

2. Build with Maven:
   ```
   mvn clean package
   ```

3. The executable JAR will be created in the `target` directory:
   ```
   target/oneui-porter-7.0-jar-with-dependencies.jar
   ```

## Usage

1. Launch the application:
   ```
   java -jar oneui-porter-7.0-jar-with-dependencies.jar
   ```

2. Select the source ROM (the ROM you want to port from)
3. Select the target ROM (the ROM you want to port to)
4. Configure porting options
5. Start the porting process
6. The ported ROM will be created in the specified output directory

## Supported ROM Types

- OTA packages (.zip files with payload.bin)
- Samsung firmware (.tar.md5 files)

## Supported Devices

- Samsung Galaxy S series (S22, S23, S24)
- Samsung Galaxy Note series
- Samsung Galaxy Z series (Fold, Flip)
- Other Samsung devices running OneUI 7 on Android 15

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Disclaimer

This tool is provided for educational and development purposes only. Use at your own risk. The developers are not responsible for any damage to your device or loss of data that may occur from using this tool.

## Acknowledgements

- [payload-dumper-go](https://github.com/ssut/payload-dumper-go) - For OTA payload extraction
- [Android Open Source Project](https://source.android.com/) - For Android tools and documentation
- [Samsung](https://www.samsung.com/) - For OneUI and Android development

