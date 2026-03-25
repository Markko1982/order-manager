package com.example.ordermanager.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_whenEmailExists_returnsUser() {
        User user = new User();
        user.setName("Fulano");
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.USER);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        var loadedUser = userDetailsService.loadUserByUsername("user@example.com");

        assertSame(user, loadedUser);
        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void loadUserByUsername_whenEmailDoesNotExist_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        var exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@example.com"));

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userRepository).findByEmail("missing@example.com");
    }
}