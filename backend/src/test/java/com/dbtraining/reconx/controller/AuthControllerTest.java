package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.api.GlobalExceptionHandler;
import com.dbtraining.reconx.repository.AppUserRepository;
import com.dbtraining.reconx.repository.entity.AppUser;
import com.dbtraining.reconx.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AuthControllerTest {

    private final Map<String, AppUser> usersByEmail = new HashMap<>();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        usersByEmail.clear();
        AppUserRepository users = (AppUserRepository) Proxy.newProxyInstance(
                AppUserRepository.class.getClassLoader(),
                new Class<?>[]{AppUserRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByEmail" -> Optional.ofNullable(usersByEmail.get((String) args[0]));
                    case "toString" -> "InMemoryAppUserRepository";
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        AuthController controller = new AuthController(
                users,
                new BCryptPasswordEncoder(4),
                new JwtTokenProvider("test-secret-that-is-at-least-32-bytes-long", 60, "reconx-test"));
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginReturnsBearerTokenForValidCredentials() throws Exception {
        usersByEmail.put("trader@db.com", user("trader@db.com", "trader123", "TRADER", true));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"trader@db.com\",\"password\":\"trader123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", not(blankOrNullString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3_600))
                .andExpect(jsonPath("$.role").value("TRADER"));
    }

    @Test
    void loginReturns401WithoutRevealingWhyCredentialsFailed() throws Exception {
        usersByEmail.put("trader@db.com", user("trader@db.com", "trader123", "TRADER", true));
        usersByEmail.put("disabled@db.com", user("disabled@db.com", "trader123", "TRADER", false));

        assertUnauthorized("trader@db.com", "wrong");
        assertUnauthorized("unknown@db.com", "trader123");
        assertUnauthorized("disabled@db.com", "trader123");
    }

    @Test
    void loginRejectsInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private void assertUnauthorized(String email, String password) throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid credentials"));
    }

    private static AppUser user(String email, String password, String role, boolean enabled) {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "email", email);
        ReflectionTestUtils.setField(user, "passwordHash", new BCryptPasswordEncoder(4).encode(password));
        ReflectionTestUtils.setField(user, "role", role);
        ReflectionTestUtils.setField(user, "enabled", enabled);
        return user;
    }
}
