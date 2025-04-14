# Cambios realizados para la version 1.1.0

## Menu bar

Para añadir una barra de menu he creado un fragmento nuevo dentro de **fragments.html**:

```html
<div>
    <nav th:fragment="navbar" class="navbar navbar-expand-lg navbar-light bg-light">
        <a class="navbar-brand" href="/about">ToDoList</a>
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarText"
                aria-controls="navbarText" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarText">
            <ul class="navbar-nav mr-auto">
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/usuarios/{id}/tareas(id=${#session.getAttribute('idUsuarioLogeado')})}">Tasks</a>
                </li>
            </ul>
            <span th:if="${userLoggedIn}" class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="#" id="navbarDropdownMenuLink" data-toggle="dropdown"
                   aria-haspopup="true" aria-expanded="false" th:text="${#session.getAttribute('nombreUsuarioLogeado')}">
                    Username
                </a>
                <div class="dropdown-menu" aria-labelledby="navbarDropdownMenuLink">
                    <a class="dropdown-item" href="#">Account</a>
                    <a class="dropdown-item" href="/logout">
                        Log out <span th:text="${#session.getAttribute('nombreUsuarioLogeado')}"></span>
                    </a>
                </div>
            </span>
            <span th:unless="${userLoggedIn}" class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="#" id="guestDropdownMenuLink" data-toggle="dropdown"
                   aria-haspopup="true" aria-expanded="false">
                    Guest
                </a>
                <div class="dropdown-menu" aria-labelledby="guestDropdownMenuLink">
                    <a class="dropdown-item" href="/login">Login</a>
                    <a class="dropdown-item" href="/registro">Registration</a>
                </div>
            </span>
        </div>
    </nav>
</div>
```

Por defecto, la barra de menu muestra "Guest" (Invitado) y, despues de inicio de session, el nombre de usuario logueado. Para saber si el usuario está logueado o no, al modelo se le pasa una variable correspondiente (**userLoggedIn**) dentro de **HomeController**:

```java
@ModelAttribute
    public void addAttributes(Model model) {
        Long userId = managerUserSession.getIdUsuario();
        if (userId != null) {
            model.addAttribute("userLoggedIn", true);
            model.addAttribute("userId", userId);
        }
        else {
            model.addAttribute("userLoggedIn", false);
        }
```

En cada plantilla HTML he incorporado este fragmento añadiendo la siguiente línea al inicio de <body>:

```html
<div th:replace="fragments :: navbar"></div>
```

## List of users

Para crear la lista de usuarios registrados se ha creado el template registered.html. A este template se le ha añadido la columna Admin con valores de tipo booleano para poder identificar de manera más sencilla al admin user. 

```html
<div class="col">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th>Id</th>
                    <th>Admin</th>
                    <th>Email</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr th:each="usuario: ${usuarios}">
                    <td th:text="${usuario.id}"></td>
                    <td th:text="${usuario.admin}"></td>
                    <td th:text="${usuario.email}"></td>
                    <td><a class="btn btn-link" th:href="@{/registered/{id}(id=${usuario.id})}">Details</a></td>
                </tr>
                </tbody>
            </table>
            <a class="btn btn-secondary" href="/about">Volver</a></p>
        </div>
```

A este template se le pasa un List<UsuarioData> usuarios desde el controlador UsuariosController:

```java
@GetMapping("/registered")
    public String registeredList(Model model) {
        List<UsuarioData> usuarios = usuarioService.getAllUsers();
        model.addAttribute("usuarios", usuarios);
        return "registered";
    }
```

**Importante**: pasamos los objetos de usuarios con todos los campos, es decir incluso la contraseña. **Esto no es seguro** y habrá que arreglarlo posteriormente creando segundo dto para Usuario.

Dicha lista se obtiene usando metodo getAllUsers de UsuarioRepository que saca el conjunto de objetos Usuario, los ordena y formatea:

```java
@Transactional(readOnly = true)
    public List<UsuarioData> getAllUsers() {
        logger.debug("Devolviendo el listado de todos usuarios registrados ");

        return StreamSupport.stream(usuarioRepository.findAll().spliterator(), false)
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .sorted((a, b) -> a.getId() < b.getId() ? -1 : a.getId() == b.getId() ? 0 : 1)
                .collect(Collectors.toList());
    }
```

## User description page

Esta página es accesible unicamente desde la pagina de usuarios registrados.  Para cada usuario hay un link Details para acceder a la ficha de usuario:

```html
<a class="btn btn-link" th:href="@{/registered/{id}(id=${usuario.id})}">Details</a>
```

Este link pasa el id de usuario seleccionado al endpoint controlado por el UsuariosController:

```java
@GetMapping("/registered/{id}")
    public String registeredUser(@PathVariable(value="id") Long idUsuario, Model model) {

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("id", usuario.getId());
        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("email", usuario.getEmail());
        model.addAttribute("fechaNacimiento", usuario.getFechaNacimientoFormateada());
        return "userPage";
    }
```

Dicho controlador saca de la bd los datos del usuario con este id y pasa sus datos como strings al template. Para la página de usuario se ha creado el template userPage.html:

```html
<div class="card-header bg-primary text-white text-center">
   <h2 th:text="${nombre}">Username</h2>
</div>
<div class="card-body">
    <ul class="list-group list-group-flush">
       <li class="list-group-item">
          <strong>ID: </strong> <span th:text="${id}"></span>
       </li>
       <li class="list-group-item">
          <strong>Email: </strong> <span th:text="${email}"></span>
       </li>
       <li class="list-group-item">
          <strong>Name: </strong> <span th:text="${nombre}">Not specified</span>
       </li>
       <li class="list-group-item">
          <strong>Birthdate: </strong> <span th:text="${fechaNacimiento}">Not specified</span>
        </li>
   </ul>
</div>
```

Puesto que los datos tipos nombre y fecha de nacimiento no son obligatorios y pueden ser null, se muestra la frase "Not specified".

## Admin user

Para añadir un rol de admin se ha añadido un atributo booleano "admin" al RegistroData, modelo Usuario y al dto UsuarioData.

```java
private Boolean admin;

public Boolean getAdmin() {
        return admin;
    }
public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
```

En el caso de RegistroData, ahora tiene un constructor nuevo que se inicializa con admin=false:

```java
public RegistroData() {
        this.admin = false;
    }
```

Además, ha sido creado un método que verifica si ya existe el admin en el UsuarioRepository:

```java
boolean existsByAdminTrue();
```

 En el template formRegistro.html se ha añadido un check box para registrarse como admin. Este check box es visible solo mientras en bd no existe el usuario con admin=true:

```html
<div th:unless="${adminExists}" class="form-check mb-3">
    <input type="checkbox" class="form-check-input" id="admin_checkbox" th:field="*{admin}">
    <label class="form-check-label" for="admin_checkbox">Registrar como admin</label>
</div>
```

El atributo adminExists se gestiona por el LoginController y se pasa a la vista en endpoint /registro:

```java
@GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("registroData", new RegistroData());
        model.addAttribute("adminExists", usuarioRepository.existsByAdminTrue());
        return "formRegistro";
    }
```

Cuando se envia el formulario de registro, el LoginController verifica si ha sido marcado el check box y si existe el admin en la bd antes de registrar al usuario y redireccionarle al login:

```java
@PostMapping("/registro")
   public String registroSubmit(@Valid RegistroData registroData, BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "formRegistro";
        }

        if (usuarioService.findByEmail(registroData.getEmail()) != null) {
            model.addAttribute("registroData", registroData);
            model.addAttribute("error", "El usuario " + registroData.getEmail() + " ya existe");
            return "formRegistro";
        }
       if (registroData.getAdmin() && usuarioService.existsByAdminTrue()) {
           model.addAttribute("registroData", registroData);
           model.addAttribute("error", "El admin ya existe");
           return "formRegistro";
       }

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail(registroData.getEmail());
        usuario.setPassword(registroData.getPassword());
        usuario.setFechaNacimiento(registroData.getFechaNacimiento());
        usuario.setNombre(registroData.getNombre());
        usuario.setAdmin(registroData.getAdmin());

        usuarioService.registrar(usuario);

        return "redirect:/login";

   }
```

Ahora, si el usuario logueado es de tipo admin, el metodo loginSubmit le redirecciona a la lista de usuarios registrados (/registered) en vez de la pagina de tareas:

```java
if (loginStatus == UsuarioService.LoginStatus.LOGIN_OK) {
            UsuarioData usuario = usuarioService.findByEmail(loginData.geteMail());
            managerUserSession.logearUsuario(usuario.getId(),usuario.getNombre());
            return usuario.getAdmin() ? "redirect:/registered" : "redirect:/usuarios/" + managerUserSession.usuarioLogeado() + "/tareas";
```

---



# Tests añadidos

AboutPageTest.java:

- getAboutDevuelveNombreAplicacion - verifica que se carga la "/about"

- getAboutGuestNavbar - verifica que navbar se carga en la "/about"

RegisteredPageTest.java

- getRegisteredPage - verifica que se carga la "/registered"

UsuarioWebTest.java:

- servicioPerfilUsuarioOK - verifica el login y la ficha de usuario

- noMostrarCheckboxAdminSiYaExisteUnAdmin - verifica que el checkbox de admin no aprece en la página de registración si ya existe el admin
