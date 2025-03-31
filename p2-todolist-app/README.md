# To Do List app

ToDoList app usign Spring Boot and Thymeleaf templates.

## Requirements

You need install on your system:

- Java 8 SDK

## Ejecución

You can run the app using the goal `run` from Maven's _plugin_ 
on Spring Boot:

```
$ ./mvn spring-boot:run 
```   

You can already create a `jar` file and run it:

```
$ ./mvn package
$ java -jar target/todolist-inicial-0.0.1-SNAPSHOT.jar 
```

Once the app is running, you can open your favourite browser and connect to:

- [http://localhost:8080/login](http://localhost:8080/login)
- [http://localhost:8080/about](http://localhost:8080/about)
- [http://localhost:8080/registered](http://localhost:8080/registered)



## More

- Trello board link : 	https://trello.com/b/7wbBqfUC/p2-to-do-list-app 
- GitHub link :			https://github.com/Serein712/ATSD_2025_P2
- Docker Hub link : 	https://hub.docker.com/r/alehsumin51/p2-todolistapp
- - Docker Pull Command:	`docker pull alehsumin51/p2-todolistapp`

## New
- Added:
- - HTML `/registered.html`
- - Controller UsuariosController with `registeredList`
- - Service UsuariosService with `getAllUsers`
- - Test RegisteredPageTest.java 
- - Test for menu navbar in AboutPageTest.java
- - Docker image updated