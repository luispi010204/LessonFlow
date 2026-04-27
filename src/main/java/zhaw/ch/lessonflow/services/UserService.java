package zhaw.ch.lessonflow.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Service
public class UserService {

    public boolean userHasRole(String role) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> userRoles = jwt.getClaimAsStringList("user_roles");

        if (userRoles == null) {
            return false;
        }

        return userRoles.contains(role);
    }

    public String getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getSubject();
    }

    public String getEmail() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaimAsString("email");
    }
}