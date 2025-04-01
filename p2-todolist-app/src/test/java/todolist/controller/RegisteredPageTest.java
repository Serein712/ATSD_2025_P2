package todolist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class RegisteredPageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getRegisteredPage() throws Exception {
        this.mockMvc.perform(get("/registered"))
                .andExpect(content().string(containsString("Usuarios")));
    }

    @Test
    public void getRegisteredUserPage() throws Exception {
        this.mockMvc.perform(get("/registered/1"))
                .andExpect(content().string(allOf(
                        containsString("Richard Stallman"),
                        containsString("richard"))));
    }

}
