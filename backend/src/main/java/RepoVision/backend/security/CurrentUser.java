package RepoVision.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import RepoVision.backend.exceptions.UnauthorizedException;


@Component
public class CurrentUser {
     public AppUserPrincipal require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication(); // returns info of authencated user from security context holder
        if (auth == null || !(auth.getPrincipal() instanceof AppUserPrincipal principal)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return principal;
    }
}
