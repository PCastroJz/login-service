package com.backend.sistembjc.service;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.backend.sistembjc.models.User;
import com.backend.sistembjc.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ConfirmationTokenService confirmationTokenService;

    @Autowired
    private EmailService emailService;

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String METHOD = "loadUserByUsername - ";

        logger.info(METHOD + "Inicia el metodo");

        try {
            logger.info(METHOD + "Se busca al usuario con el email: " + username);

            User user = userRepository.findByEmail(username)
                    .orElseThrow(
                            () -> new UsernameNotFoundException("No se encontro usuario con el email " + username));

            return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                    List.of());
        } catch (UsernameNotFoundException e) {
            logger.error(METHOD + "Usuario no encontrado: " + username, e);
            throw e;
        }

    }

    public User register(User user) {
        String METHOD = "registrar - ";

        logger.info(METHOD + "Inicia el metodo");
        logger.info(METHOD + "Comienza el registro del usuario " + user.getName());

        logger.info(METHOD + "Se encripta la contraseña del usuario " + user.getName());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);

        logger.info(METHOD + "Se guarda al usuario en la BD");
        User savedUser = userRepository.save(user);

        logger.info(METHOD + "Se comienza la creacion del token para confirmar por correo");
        String token = confirmationTokenService.createConfirmationToken(user);

        String link = "http://localhost:8080/users/confirm?token=" + token;

        logger.info(METHOD + "Se envia el token al email del usuario: " + user.getEmail());
        emailService.send(user.getEmail(), buildEmail(user.getName(), link));

        return savedUser;

    }

    public User updateEmail(Long userId, String newEmail) {
        String METHOD = "updateEmail - ";

        logger.info(METHOD + "Inicia el metodo");

        try {

            logger.info(METHOD + "Se busca el id del usuario: " + userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("No se encontro al usuario"));

            logger.info(METHOD + "Se cambia el email del usuario de " + user.getEmail() + " a " + newEmail + ".");
            user.setEmail(newEmail);
            logger.info(METHOD + "Se guarda al usuario en la BD");
            return userRepository.save(user);
        } catch (IllegalStateException e) {
            logger.error(METHOD + "Usuario no encontrado: " + userId, e);
            throw e;
        }
    }

    public User updatePassword(Long userId, String newPassword) {
        String METHOD = "updatePassword - ";

        logger.info(METHOD + "Inicia el metodo");

        try {
            logger.info(METHOD + "Se busca el id del usuario: " + userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("No se encontro al usuario"));

            user.setPassword(passwordEncoder.encode(newPassword));
            logger.info(METHOD + "Se guarda al usuario en la BD");
            return userRepository.save(user);
        } catch (IllegalStateException e) {
            logger.error(METHOD + "Usuario no encontrado: " + userId, e);
            throw e;
        }
    }

    public void deleteUser(Long userId) {
        String METHOD = "deleteUser - ";

        logger.info(METHOD + "Inicia el metodo");

        try {
            logger.info(METHOD + "Se busca el id del usuario: " + userId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("No se encontro al usuario"));

            userRepository.deleteById(user.getId());
            logger.info(METHOD + "Se elimina al usuario con id: " + userId);
        } catch (IllegalStateException e) {
            logger.error(METHOD, "Usuario no encontrado: " + userId, e);
            throw e;
        }
    }

    public User findByEmail(String email) {
        String METHOD = "findByEmail - ";

        logger.info(METHOD + "Inicia el metodo");

        try {
            logger.info(METHOD + "Se encontro al usuario");
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("No se encontro al usuario"));
        } catch (IllegalStateException e) {
            logger.error(METHOD, "Usuario no encontrado: " + email, e);
            throw e;
        }
    }

    public void enableUser(Long userId) {
        String METHOD = "enableUser - ";

        logger.info(METHOD + "Inicia el metodo");
        try {
            logger.info(METHOD + "Se habilito al usuario");
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
            user.setEnabled(true);

            userRepository.save(user);
        } catch (IllegalStateException e) {
            logger.error(METHOD, "Usuario no encontrado: " + userId, e);
            throw e;
        }
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    private String buildEmail(String name, String link) {
        return "<div>Hola " + name + ", </div>" +
                "<div>Por favor haz click en el siguiente enlace para confirmar tu correo electronico: </div>"
                + "<div><a href=\"" + link + "\">Confirmar correo electronico</a></div>";
    }
}
