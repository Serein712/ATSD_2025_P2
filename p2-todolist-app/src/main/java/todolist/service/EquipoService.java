package todolist.service;

import todolist.dto.EquipoData;
import todolist.dto.UsuarioData;
import todolist.model.Equipo;
import todolist.model.Usuario;
import todolist.repository.EquipoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import todolist.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;


    // Se añade un equipo en la aplicación.
    // El nombre debe ser distinto de null
    // El nombre no debe estar registrado en la base de datos
    @Transactional
    public EquipoData registrar(EquipoData equipo) {
        Optional<Equipo> equipoBD = equipoRepository.findByNombre(equipo.getNombre());
        if (equipoBD.isPresent())
            throw new EquipoServiceException("El equipo " + equipo.getNombre() + " ya está registrado");
        else if (equipo.getNombre() == null)
            throw new EquipoServiceException("El equipo no tiene nombre");
        else {
            Equipo equipoNuevo = modelMapper.map(equipo, Equipo.class);
            equipoNuevo = equipoRepository.save(equipoNuevo);
            return modelMapper.map(equipoNuevo, EquipoData.class);
        }
    }

    @Transactional(readOnly = true)
    public EquipoData findByNombre(String nombre) {
        Equipo equipo = equipoRepository.findByNombre(nombre).orElse(null);
        if (equipo == null) return null;
        else {
            return modelMapper.map(equipo, EquipoData.class);
        }
    }

    @Transactional(readOnly = true)
    public EquipoData findById(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);
        if (equipo == null) return null;
        else {
            return modelMapper.map(equipo, EquipoData.class);
        }
    }

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        Optional<Equipo> equipoBD = equipoRepository.findByNombre(nombre);
        if (equipoBD.isPresent())
            throw new EquipoServiceException("El equipo " + nombre + " ya está registrado");
        else if (nombre == null)
            throw new EquipoServiceException("El equipo no tiene nombre");
        else {
            Equipo equipoNuevo = modelMapper.map(new Equipo(), Equipo.class);
            equipoNuevo.setNombre(nombre);
            equipoNuevo = equipoRepository.save(equipoNuevo);

            return modelMapper.map(equipoNuevo, EquipoData.class);
        }
    }

    @Transactional
    public EquipoData recuperarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);

        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional
    public List<EquipoData> findAllOrdenadoPorNombre() {
        // recuperamos todos los equipos
        List<Equipo> equipos;
        equipos = equipoRepository.findAll();

        // cambiamos el tipo de la lista de equipos
        List<EquipoData> equiposData = equipos.stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());

        // ordenamos la lista por nombre del equipo
        Collections.sort(equiposData, (a, b) -> a.getNombre().compareTo(b.getNombre()));
        return equiposData;
    }

    @Transactional
    public void añadirUsuarioAEquipo(Long idEquipo, Long idUsuario) {
        // recuperamos el equipo
        Equipo equipo = equipoRepository.findById(idEquipo).orElse(null);
        if (equipo == null) throw new EquipoServiceException("El equipo no existe");

        // recuperamos el usuario
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) throw new EquipoServiceException("El usuario no existe");

        // comprobamos que el usuario no pertenece al equipo
        if (equipo.getUsuarios().contains(usuario))
            throw new EquipoServiceException("El usuario ya pertenece al equipo");

        // añadimos el usuario al equipo
        equipo.addUsuario(usuario);
        // guardamos el equipo
        equipoRepository.save(equipo);
        // guardamos el usuario
        usuarioRepository.save(usuario);
        // con ello se guarda la relación
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        // Hacemos uso de Java Stream API para mapear la lista de entidades a DTOs.
        return equipo.getUsuarios().stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<EquipoData> equiposUsuario(long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) return new ArrayList<>();

        // cambiamos el tipo de la lista de equipos
        List<EquipoData> equipos = usuario.getEquipos().stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());
        return equipos;

    }
}
