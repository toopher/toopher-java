# ToopherAPI Java Client

#### Java Version
>=5.0 (originally numbered 1.5)

#### Installing Dependencies
Toopher uses [Maven](http://maven.apache.org/).

To install Maven with Homebrew run:
```shell
$ brew install maven
```

#### Tests
To run the tests enter:
```shell
$ mvn test
```

To get coverage reports with the JaCoCo Maven Plugin:
```shell
$ mvn clean verify -P all-tests
$ open target/site/jacoco/index.html
```