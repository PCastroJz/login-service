package com.backend.sistembjc.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.sistembjc.models.ConfirmationToken;
import com.backend.sistembjc.models.User;
import com.backend.sistembjc.repository.ConfirmationTokenRepository;

@Service
public class ConfirmationTokenService {

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    public String createConfirmationToken(User user) {
        String METHOD = "createConfirmationToken - ";

        logger.info(METHOD + "Inicia el metodo");

        logger.info(METHOD + "Se crea el token");
        String token = UUID.randomUUID().toString();
        logger.info(METHOD + "Token: "+token);
        ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15), user);

        saveConfirmationToken(confirmationToken);

        return token;
    }

    public void saveConfirmationToken(ConfirmationToken token) {
        confirmationTokenRepository.save(token);
    }

    public Optional<ConfirmationToken> getToken(String token) {
        return confirmationTokenRepository.findByToken(token);
    }

    public void setConfirmedAt(String token) {
        ConfirmationToken confirmationToken = getToken(token)
                .orElseThrow(() -> new IllegalStateException("Token no encontrado"));

        confirmationToken.setConfirmedAt(LocalDateTime.now());

        confirmationTokenRepository.save(confirmationToken);
    }

}
