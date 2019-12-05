# Microservice Workspaces

Microservice that is responsible for the management of a municipality and at the same time orchestrates all other microservices.

## Running Development

```sh
$ mvn spring-boot:run
```

### How to disable eureka client?

Modify the **enabled** property in st-microservice-workspaces/src/main/resources/**application.yml** file:

```yml
eureka:
  client:
    enabled: false
```

### How to disable config client?

Modify the **enabled** property in st-microservice-workspaces/src/main/resources/**bootstrap.yml** file:

```yml
spring:
  application:
    name: st-microservice-workspaces
  cloud:
    config:
      enabled: false
```

## Running Production

### Master Branch

Go to the master branch

```sh
$ git checkout master
```

### Generate jar

```sh
$ mvn clean package -DskipTests
```

### Create Network Docker

```sh
$ docker network create st
```

### Create image from Dockerfile

```sh
$ docker build -t st-microservice-workspaces:ursus .
```

### Run Container

```sh
$ docker run -P -t --network st -d st-microservice-workspaces:ursus
```

## License

[Agencia de Implementación - BSF Swissphoto - INCIGE](https://github.com/AgenciaImplementacion/st-microservice-workspaces/blob/master/LICENSE)