# Case Report
Provides functionality for generating and submitting case reports for patients.

#### Table of Contents

1. [Build](#build)
   1. [Prerequisites](#prerequisites)
   2. [Build Command](#build-command)

## Build
### Prerequisites

#### Java

The module is a Java application which is why you need to install a Java JDK.

If you want to build the master branch you will need Java 8.

#### OpenMRS
Case Based Reporting module 2.0 was tested using OpenMRS version 2.2/2.3

P.S: Its important to use Reporting Compatibility OMOD version 2.0.8-SNAPSHOT or higher.

#### Maven

Install the build tool [Maven](https://maven.apache.org/).

You need to ensure that Maven uses the Java JDK needed.

To do so execute

```bash
mvn -version
```

which will tell you what version Maven is using. Refer to the [Maven docs](https://maven.apache.org/configure.html) if you need to configure Maven.

#### Git

Install the version control tool [git](https://git-scm.com/) and clone this repository with

```bash
git clone https://github.com/openmrs/openmrs-module-casereport.git
```

### Build Command

After you have taken care of the [Prerequisites](#prerequisites)

Execute the following from the command line.
(Note: some CBR 1.0 test code needs to be cleaned, so use -DskipTests to create OMOD).

```bash
cd openmrs-module-casereport
mvn clean install -DskipTests
or mvn clean install
```
