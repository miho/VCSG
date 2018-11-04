# VCSG

[ ![Download](https://api.bintray.com/packages/miho/JCSG/VCSG/images/download.svg) ](https://bintray.com/miho/JCSG/VCSG/_latestVersion)

CSG library (uses [native CAD libraries](https://github.com/miho/OCC-CSG) as well as [JCSG](https://github.com/miho/JCSG) &amp; [VVecMath](https://github.com/miho/VVecMath))

## Building VCSG

### Requirements

- JDK >= 1.8
- Internet connection (dependencies are downloaded automatically)
- IDE: [Gradle](http://www.gradle.org/) Plugin (not necessary for command line usage)

### IDE

Open the `VCSG` core [Gradle](http://www.gradle.org/) project in your favourite IDE (tested with NetBeans 8.2 and IntelliJ 2018) and build it
by calling the `assemble` task.

### Command Line

Navigate to the `VCSG` core [Gradle](http://www.gradle.org/) project (i.e., `path/to/VCSG`) and enter the following command

#### Bash (Linux/macOS/Cygwin/other Unix shell)

    bash gradlew assemble
    
#### Windows (CMD)

    gradlew assemble 
