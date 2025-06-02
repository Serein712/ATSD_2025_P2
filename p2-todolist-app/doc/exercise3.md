# Cambios realizados para la version 1.2.0

## PostgreSQL database

Es la tabla Usuarios que almacena la información básica de los usuarios registrados en el sistema.

<img title="" src="file:///C:/Users/oleg_/AppData/Roaming/marktext/images/2025-06-02-13-15-06-image.png" alt="" data-align="left">

Esta tabla mustra distintos equipos creados por los usuarios registrados:

![](C:\Users\oleg_\AppData\Roaming\marktext\images\2025-06-02-13-16-09-image.png)

La tabla de relacion equipos-usuarios que gestiona la relación de muchos-a-muchos entre usuarios y equipos.:

![](C:\Users\oleg_\AppData\Roaming\marktext\images\2025-06-02-13-17-31-image.png)

## Nuevos endpoints

A continuación vienen explicados los puntos de entrada de la web añadidos para las user story 009 y 010.

### Crear nuevo equipo

#### Controller

Este método muestra el formulario para crear un nuevo equipo, obteniendo el usuario correspondiente por su ID y añadiéndolo al modelo.

```java
@GetMapping("/usuarios/{id}/equipos/nuevo")
    public String formNuevoEquipo(@PathVariable(value="id") Long idUsuario,
                                  @ModelAttribute EquipoData equipoData, Model model,
                                  HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);
        return "formNuevoEquipo";
    }
```

Procesa el formulario de creación de equipo, registrando el nuevo equipo y asignando al usuario creador como miembro.

```java
@PostMapping("/usuarios/{id}/equipos/nuevo")
    public String nuevoEquipo(@PathVariable(value="id") Long idUsuario,
                                  @ModelAttribute EquipoData equipoData, Model model,
                                  RedirectAttributes flash, HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        EquipoData equipoRegistrado = equipoService.registrar(equipoData);
        equipoService.añadirUsuarioAEquipo(equipoRegistrado.getId(), idUsuario);

        flash.addFlashAttribute("mensaje", "Equipo creado correctamente");
        return "redirect:/usuarios/" + idUsuario + "/equipos";
    }
```

#### Vista

La parte de formulario HTML con Thymeleaf para crear un nuevo equipo, con campo obligatorio de nombre y botones para enviar o cancelar.

```html
<div class="container-fluid">
    <h2 th:text="'Nuevo equipo para el usuario ' + ${usuario.getNombre()}"></h2>
    <form method="post" th:action="@{/usuarios/{id}/equipos/nuevo(id=${usuario.id})}" th:object="${equipoData}">
        <div class="col-6">
        <div class="form-group">
            <label for="nombre">Nombre del equipo:</label>
            <input class="form-control" id="nombre" name="nombre" required th:field="*{nombre}" type="text"/>
        </div>
        <button class="btn btn-primary" type="submit">Crear equipo</button>
        <a class="btn btn-link" th:href="@{/usuarios/{id}/equipos(id=${usuario.id})}">Cancelar</a>
        </div>
    </form>
</div>
```

#### Web Test

Test que verifica que se puede crear un equipo nuevo correctamente y que su nombre aparece en la lista tras redirigir a la pagina con equipos.

```java
@Test
    public void formNuevoEquipoTest() throws Exception {
        Long usuarioId = addUsuarioEquiposBD().get("usuarioId");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPost = "/usuarios/" + usuarioId.toString() + "/equipos/nuevo";
        String urlRedirect = "/usuarios/" + usuarioId.toString() + "/equipos";

        this.mockMvc.perform(post(urlPost)
                        .param("nombre", "TestNuevoEquipo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get(urlRedirect))
                .andExpect((content().string(containsString("TestNuevoEquipo"))));
    }
```

---

### Unirse al equipo

#### Controller

Este método permite a un usuario unirse a un equipo específico. Primero se verifica si está logueado y luego se lo añade al equipo.

```java
@PostMapping("/usuarios/{id}/equipos/{id_equipo}/unirse")
    public String unirseAlEquipo(@PathVariable(value="id") Long idUsuario,
                                 @PathVariable(value="id_equipo") Long idEquipo,
                                 Model model, RedirectAttributes flash,
                                 HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);
        equipoService.añadirUsuarioAEquipo(idEquipo, idUsuario);

        flash.addFlashAttribute("mensaje", "Se ha unido al equipo ");
        return "redirect:/equipos/" + idEquipo;}
```

#### Vista

Botón de formulario que permite al usuario unirse al equipo con una simple petición POST al backend. Este botón se encuentra el la página de listado de equipos al lado de cada equipo.

```html
<form method="post" 
     th:action="@{/usuarios/{id}/equipos/{id_equipo}/unirse(id=${usuario.id},id_equipo=${equipo.id})}">
     <button class="btn btn-primary btn-sm me-2" type="submit">
     Unirse</button>
</form>
```

#### Test

Este test de web comprueba que un nuevo usuario puede unirse a un equipo, y que su nombre y email aparecen luego entre los miembros del equipo.

```java
@Test
    public void unirseAlEquipoTest() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setNombre("no_Richard");
        usuario.setEmail("no_richard@umh.es");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        Long equipoId = addUsuarioEquiposBD().get("equipoId");

        String urlPost = "/usuarios/" + usuario.getId().toString() + "/equipos/" + equipoId + "/unirse";
        String urlRedirect = "/equipos/" + equipoId;

        this.mockMvc.perform(post(urlPost)
                        .param(String.valueOf(usuario.getId()), "id")
                        .param(String.valueOf(equipoId), "id_equipo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get("/equipos/" + equipoId))
                .andExpect(content().string(
                        allOf(containsString("Miembros de este equipo"),
                                containsString("no_Richard"),
                                containsString("no_richard@umh.es"))));
    }
```

---

### Abandonar equipo

#### Controller

Este método permite a un usuario abandonar un equipo. Se comprueba que esté logueado, se lo elimina del equipo y se muestra un mensaje de confirmación.

```java
@PostMapping("/usuarios/{id}/equipos/{id_equipo}/abandonar")
    public String abandonarEquipo(@PathVariable(value="id") Long idUsuario,
                                  @PathVariable(value="id_equipo") Long idEquipo,
                                  Model model, RedirectAttributes flash,
                                  HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);
        equipoService.quitarUsuarioDeEquipo(idEquipo, idUsuario);

        flash.addFlashAttribute("mensaje", "Ha abandonado este equipo ");
        return "redirect:/equipos/" + idEquipo;
    }
```

#### Servicio

Este servicio elimina a un usuario de un equipo, validando previamente que tanto el usuario como el equipo existen y que están relacionados.

```java
@Transactional
    public void quitarUsuarioDeEquipo(Long idEquipo, Long idUsuario) {
        // recuperamos el equipo
        Equipo equipo = equipoRepository.findById(idEquipo).orElse(null);
        if (equipo == null) throw new EquipoServiceException("El equipo no existe");

        // recuperamos el usuario
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) throw new EquipoServiceException("El usuario no existe");

        // comprobamos que el usuario no pertenece al equipo
        if (!equipo.getUsuarios().contains(usuario))
            throw new EquipoServiceException("El usuario no pertenece al equipo");

        // añadimos el usuario al equipo
        equipo.removeUsuario(usuario);
        // guardamos el equipo
        equipoRepository.save(equipo);
        // guardamos el usuario
        usuarioRepository.save(usuario);
        // con ello se guarda la relación
    }
```

#### Test

Este web test garantiza que un usuario puede abandonar un equipo desde la web y que su información ya no aparece entre los miembros del mismo.

```java
@Test
    public void abandonarEquipoTest() throws Exception {
        //Crear nuevo equipo
        EquipoData equipo = new EquipoData();
        equipo.setNombre("TestAbandonarEquipo");
        equipo.setId(2L);
        equipo = equipoService.registrar(equipo);

        //Añadir Richard al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(),addUsuarioEquiposBD().get("usuarioId"));

        //Crear nuevo usuario no_Richard
        UsuarioData usuario = new UsuarioData();
        usuario.setNombre("no_Richard");
        usuario.setEmail("no_richard@umh.es");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        //Añadir no_Richard al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(),usuario.getId());

        //Comprobar que ambos estan en el equipo
        this.mockMvc.perform(get("/equipos/" + equipo.getId()))
                .andExpect(content().string(
                        allOf(containsString("Miembros de este equipo"),
                                containsString("Richard"),
                                containsString("no_Richard"))));

        //Abandonar el grupo (cuenta de no_Richard)
        String urlPost = "/usuarios/" + usuario.getId().toString() + "/equipos/" + equipo.getId() + "/abandonar";
        String urlRedirect = "/equipos/" + equipo.getId();

        this.mockMvc.perform(post(urlPost)
                        .param(String.valueOf(usuario.getId()), "id")
                        .param(String.valueOf(equipo.getId()), "id_equipo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // Comprobar que Richard esta en el equipo y no_Richard no
        this.mockMvc.perform(get("/equipos/" + equipo.getId()))
                .andExpect(content().string(
                        allOf(containsString("Miembros de este equipo"),
                                not(containsString("no_Richard")),
                                containsString("Richard"))));
    }
```

Este test de servicio valida que al quitar a un usuario del equipo, efectivamente ya no aparece en la lista de miembros.

```java
@Test
    public void quitarUsuarioDeEquipoTest() {
        // GIVEN
        // Un usuario y un equipo en la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");

        // WHEN
        // Añadimos el usuario al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El usuario pertenece al equipo
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("user@umh");

        // WHEN
        // Quitamos el usuario del equipo
        equipoService.quitarUsuarioDeEquipo(equipo.getId(), usuario.getId());

        // THEN
        // El usuario no pertenece mas al equipo
        usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(0);
    }
```

#### Vista

Formulario con botón que permite al usuario abandonar el equipo. Se hace una petición POST al backend.

```html
<form method="post" th:action="@{/usuarios/{id}/equipos/{id_equipo}/abandonar(id=${usuario.id},id_equipo=${equipo_usuario.id})}">
  <button class="btn btn-danger btn-sm me-2" type="submit">
  Abandonar</button>
</form>
```

---



### Renombrar equipo

#### Controller

Este método muestra el formulario para renombrar un equipo, cargando el usuario y el equipo por sus respectivos IDs.

```java
@GetMapping("/usuarios/{id}/equipos/{id_equipo}/renombrar")
    public String formRenombrarEquipo(@PathVariable(value="id") Long idUsuario,
                                      @PathVariable(value="id_equipo") Long idEquipo,
                                  @ModelAttribute EquipoData equipoData, Model model,
                                  HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        EquipoData equipo = equipoService.findById(idEquipo);
        model.addAttribute("usuario", usuario);
        model.addAttribute("equipo", equipo);
        return "formRenombrarEquipo";
    }
```

Procesa el renombramiento de un equipo. Solo usuarios administradores pueden cambiar el nombre, y se valida que el nuevo nombre no esté vacío ni repetido.

```java
@PostMapping("/usuarios/{id}/equipos/{id_equipo}/renombrar")
    public String renombrarEquipo(@PathVariable(value="id") Long idUsuario,
                                        @PathVariable(value="id_equipo") Long idEquipo,
                                        @RequestParam("nombre") String nombre, Model model,
                                        RedirectAttributes flash,
                                        HttpSession session) {
        comprobarUsuarioLogeado(idUsuario);
        UsuarioData usuario = usuarioService.findById(idUsuario);
        EquipoData equipo = equipoService.findById(idEquipo);

        if (nombre == null || nombre.trim().isEmpty()) {
            flash.addFlashAttribute("mensaje", "El nombre no puede estar vacío");
            return "redirect:/equipos/" + idEquipo;}
        try {
            equipoService.renombrarEquipo(equipo,usuario,nombre);
            model.addAttribute("usuario", usuario);
            flash.addFlashAttribute("mensaje", "El equipo ha sido renombrado");
        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("mensaje", e.getMessage());
        }
        return "redirect:/equipos/" + idEquipo;
    }
```

#### Servicio

Este método de servicio permite cambiar el nombre de un equipo, siempre que lo solicite un administrador y que el nuevo nombre no esté en uso.

```java
public void renombrarEquipo(EquipoData equipo, UsuarioData usuario, String nombreB) {

        // solo admin puede renombrar un equipo
        if (usuario.getAdmin() == false)
            throw new EquipoServiceException("El usuario no es administrador");

        // comprobamos que el nombre nuevo esta libre
        if (equipoRepository.findByNombre(nombreB).isPresent())
            throw new EquipoServiceException("Este nombre ya está ocupado");

        // buscamos el equipo en BD
        Equipo equipoBD = equipoRepository.findById(equipo.getId())
                .orElseThrow(() -> new EquipoServiceException("El equipo no existe"));

        // cambiamos el nombre del equipo
        equipoBD.setNombre(nombreB);
        equipoRepository.save(equipoBD);
    }
```

#### Vista

Formulario HTML para que un administrador cambie el nombre del equipo. El nombre actual se muestra como valor por defecto.

```html
<form method="post" th:action="@{/usuarios/{id}/equipos/{id_equipo}/renombrar(id=${usuario.id},id_equipo=${equipo.id})}" th:object="${string}">
  <div class="col-6">
  <div class="form-group">
      <label for="nombre">Nombre del equipo:</label>
      <input class="form-control" id="nombre" name="nombre" th:value="${equipo.getNombre()}"  type="text"/>
  </div>
  <button class="btn btn-primary" type="submit">Renombrar equipo</button>
  <a class="btn btn-link" th:href="@{/usuarios/{id}/equipos(id=${usuario.id})}">Cancelar</a>
  </div>
</form>
```

#### Tests

Este test de servicio verifica que un usuario administrador puede renombrar correctamente un equipo.

```java
@Test
    public void renombrarEquipoTest(){

        // GIVEN
        // Un equipo en la base de datos..
        EquipoData equipo = equipoService.crearEquipo("Nombre A");
        assertThat(equipo.getNombre()).isEqualTo("Nombre A");

        // .. y un usuario admin
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario.setAdmin(true);
        usuario = usuarioService.registrar(usuario);

        // WHEN
        // se renombra el equipo
        equipoService.renombrarEquipo(equipo,usuario, "Nombre B");

        //THEN
        // el nombre del equipo ha cambiado
        EquipoData equipoActualizado = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoActualizado.getNombre()).isEqualTo("Nombre B");
    }
```

Este test comprueba que si un usuario sin permisos de administrador intenta renombrar un equipo, se lanza una excepción adecuada.

```java
@Test
    public void renombrarEquipoExcepcionTest(){

        // GIVEN
        // Un equipo en la base de datos..
        EquipoData equipo = equipoService.crearEquipo("Nombre A");
        assertThat(equipo.getNombre()).isEqualTo("Nombre A");

        // .. y un usuario no admin
        UsuarioData usuario2 = new UsuarioData();
        usuario2.setEmail("user@umh");
        usuario2.setPassword("1234");
        usuario2.setAdmin(false);
        usuario2 = usuarioService.registrar(usuario2);

        UsuarioData finalUsuario = usuario2;
        assertThatThrownBy(() ->
                equipoService.renombrarEquipo(equipo, finalUsuario, "Nombre B")
        ).isInstanceOf(EquipoServiceException.class)
                .hasMessage("El usuario no es administrador");
    }
```

---



### Disolver equipo

#### Controller

Este método permite a un administrador disolver (eliminar) un equipo. En caso de error (por permisos, por ejemplo), se muestra un mensaje apropiado.

```java
@PostMapping("/usuarios/{id}/equipos/{id_equipo}/disolver")
    public String disolverEquipo(@PathVariable(value="id") Long idUsuario,
                                  @PathVariable(value="id_equipo") Long idEquipo,
                                  Model model,
                                  RedirectAttributes flash,
                                  HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);
        UsuarioData usuario = usuarioService.findById(idUsuario);
        EquipoData equipo = equipoService.findById(idEquipo);
        try {
            equipoService.disolverEquipo(equipo,usuario);
            model.addAttribute("usuario", usuario);
            flash.addFlashAttribute("mensaje", "El equipo ha sido disuelto");

        } catch (EquipoServiceException e) {
            flash.addFlashAttribute("mensaje", e.getMessage());
        }
        return "redirect:/usuarios/" + idUsuario +"/equipos";
    }
```

#### Servicio

El servicio elimina un equipo de la base de datos, pero solo si quien realiza la operación es un administrador.

```java
public void disolverEquipo(EquipoData equipo, UsuarioData admin) {
        // solo admin puede renombrar un equipo
        if (admin.getAdmin() == false)
            throw new EquipoServiceException("El usuario no es administrador");
        equipoRepository.deleteById(equipo.getId());
    }
```

#### Vista

Formulario HTML con botón para disolver el equipo. Visible y funcional solo si el usuario tiene permisos adecuados.

```html
<form method="post" th:action="@{/usuarios/{id}/equipos/{id_equipo}/disolver(id=${usuario.id},id_equipo=${equipo_usuario.id})}">
     <button class="btn btn-danger btn-sm me-2" type="submit">Disolver</button>
</form>
```

#### Test

Este test de servicio valida que un administrador puede disolver un equipo y que, una vez eliminado, no se puede recuperar ni aparece asignado a ningún usuario.

```java
@Test
    public void disolverEquipoTest(){

        // GIVEN
        // Un equipo en la base de datos..
        EquipoData equipo = equipoService.crearEquipo("Nombre A");

        // un usuario miembro del equipo
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // .. y un usuario admin
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@umh");
        admin.setPassword("1234");
        admin.setAdmin(true);
        admin = usuarioService.registrar(admin);

        // WHEN
        // se elimina el equpo por el admin
        equipoService.disolverEquipo(equipo,admin);

        //THEN
        // el equipo no existe
        assertThatThrownBy(() ->
                equipoService.recuperarEquipo(equipo.getId())
        ).isInstanceOf(EquipoServiceException.class).hasMessage("El equipo no existe");

        // y asegurar que usuario no tiene equipo asignado
        assertThat(equipoService.equiposUsuario(usuario.getId())).isEmpty();
    }
```
