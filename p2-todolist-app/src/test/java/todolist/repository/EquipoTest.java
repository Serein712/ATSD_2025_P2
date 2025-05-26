package todolist.repository;

import todolist.model.Equipo; // This model doesn’t exist yet 
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoTest {
    @Test
    public void crearEquipo() {
        Equipo equipo = new Equipo("Project P1");
        assertThat(equipo.getNombre()).isEqualTo("Project P1");
    }
}
