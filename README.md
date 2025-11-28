# superstore-management

On Ubuntu you just need Java, Maven, then you can build and run the Maven project with the `pom.xml`. 

## 1. Install Java and Maven

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven
java -version
mvn -version
```

Use Java 17 or newer so JavaFX 21 works correctly.[2]

## 4. Build the project

From the project root (`superstore-management`):

```bash
mvn clean compile
```

To run unit tests:

```bash
mvn test
```

Maven will download JavaFX and JUnit the first time it runs.

## 5. Run the JavaFX application

In the same project folder:

```bash
mvn exec:java
```

This starts `com.superstore.ui.MainApp`, opens the login window, and creates/uses `superstore_data.dat` and `superstore.log` in the working directory for data and logging as required.
