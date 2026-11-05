package com.example.candidateservice.KeycloakUser;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserService {

    private static final Set<String> ALLOWED_ROLES = Set.of("CANDIDAT");

    private final Keycloak keycloak;
    @Value("${keycloak.admin.realm}") private String realm;

    public String createUser(String email, String firstName, String lastName,
                             String role, String tempPassword) {
        if (!ALLOWED_ROLES.contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rôle invalide");
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(tempPassword);
        cred.setTemporary(true);               // l'utilisateur change son mot de passe au 1er login
        user.setCredentials(List.of(cred));

        RealmResource realmResource = keycloak.realm(realm);

        try (Response response = realmResource.users().create(user)) {
            if (response.getStatus() == 409) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Utilisateur déjà existant");
            }
            if (response.getStatus() != 201) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Échec création Keycloak");
            }
            String userId = CreatedResponseUtil.getCreatedId(response);

            RoleRepresentation roleRep = realmResource.roles().get(role).toRepresentation();
            realmResource.users().get(userId).roles().realmLevel().add(List.of(roleRep));
            return userId;   // = claim "sub" du futur JWT
        }
    }
    public void deleteUser(String userId) {
        try (Response response = keycloak.realm(realm).users().delete(userId)) {
            int status = response.getStatus();
            if (status == 404) {
                log.warn("Utilisateur Keycloak {} introuvable, rien à supprimer", userId);
            } else if (status != 204) {
                log.error("Échec suppression Keycloak de {} : HTTP {}", userId, status);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la suppression Keycloak de {}", userId, e);
        }
    }
}
