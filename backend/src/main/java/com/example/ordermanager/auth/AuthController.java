package com.example.ordermanager.auth;

import com.example.ordermanager.auth.dto.AuthResponseDTO;
import com.example.ordermanager.auth.dto.LoginDTO;
import com.example.ordermanager.auth.dto.RegisterDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterDTO data) {

        // verifica se já existe usuário com esse e-mail
        if (userRepository.findByEmail(data.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        User user = new User();
        user.setEmail(data.email());
        user.setPassword(passwordEncoder.encode(data.password()));
        user.setRole(UserRole.USER);

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginDTO data) {

        UsernamePasswordAuthenticationToken usernamePassword =
                new UsernamePasswordAuthenticationToken(data.email(), data.password());

        Authentication auth = authenticationManager.authenticate(usernamePassword);
        User user = (User) auth.getPrincipal();

        String token = tokenService.generateToken(user);

        AuthResponseDTO response = new AuthResponseDTO(token, "Bearer");
        return ResponseEntity.ok(response);
    }
}
