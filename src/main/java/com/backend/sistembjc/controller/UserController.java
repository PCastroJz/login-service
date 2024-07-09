package com.backend.sistembjc.controller;

import org.springframework.web.bind.annotation.RestController;

import com.backend.sistembjc.models.ConfirmationToken;
import com.backend.sistembjc.models.LoginRequest;
import com.backend.sistembjc.models.User;
import com.backend.sistembjc.service.ConfirmationTokenService;
import com.backend.sistembjc.service.UserService;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ConfirmationTokenService confirmationTokenService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid User user) {
        userService.register(user);
        return new ResponseEntity<>(createResponse("Se registro al usuario, enviamos un correo de confirmación", HttpStatus.OK), HttpStatus.OK);
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirmToken(@RequestParam("token") String token) {
        
        try {
            ConfirmationToken confirmationToken = confirmationTokenService.getToken(token)
                .orElseThrow(() -> new IllegalStateException("Token no encontrado"));

                if (confirmationToken.getConfirmedAt() != null) {
                    return new ResponseEntity<>(createResponse("El token ya fue utilizado", HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
                }

                LocalDateTime expiredAt = confirmationToken.getExpireAt();
                if (expiredAt.isBefore(LocalDateTime.now())) {
                    return new ResponseEntity<>(createResponse("El token ya expiro", HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
                }

                confirmationTokenService.setConfirmedAt(token);
                User user = confirmationToken.getUser();

                userService.enableUser(user.getId());

                return new ResponseEntity<>(createResponse("Token confirmado", HttpStatus.OK), HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(createResponse(e.getMessage(), HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }
        
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.findByEmail(loginRequest.getEmail());

        if (user.isEnabled()) {
            if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return new ResponseEntity<>(createResponse("Inicio de sesión correcto", HttpStatus.OK), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(createResponse("Credenciales incorrectas", HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(createResponse("Confirma tu correo electronico para acceder", HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody LoginRequest loginRequest) {
        User user = userService.findByEmail(loginRequest.getEmail());

        userService.updatePassword(user.getId(), loginRequest.getPassword());
        return new ResponseEntity<>(createResponse("Contraseña actualizada", HttpStatus.OK), HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);

        return new ResponseEntity<>(createResponse("Usuario Eliminado", HttpStatus.OK), HttpStatus.OK);
    }

    private Map<String, Object> createResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", status.value());
        return response;
    }

}
