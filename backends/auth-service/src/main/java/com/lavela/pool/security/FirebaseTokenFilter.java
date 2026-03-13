package com.lavela.pool.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.lavela.pool.domain.entity.User;
import com.lavela.pool.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    @Value("${app.dev-auth.enabled:false}")
    private boolean devAuthEnabled;

    @Value("${app.dev-auth.bearer-token:}")
    private String devBearerToken;

    @Value("${app.dev-auth.firebase-uid:}")
    private String devFirebaseUid;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractBearerToken(request);

        if (StringUtils.hasText(token)) {
            if (devAuthEnabled && token.equals(devBearerToken)) {
                authenticateByFirebaseUid(devFirebaseUid, request);
                filterChain.doFilter(request, response);
                return;
            }

            try {
                FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
                authenticateByFirebaseUid(firebaseToken.getUid(), request);
            } catch (Exception ex) {
                log.warn("Firebase token verification failed: {}", ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void authenticateByFirebaseUid(String firebaseUid, HttpServletRequest request) {
        if (!StringUtils.hasText(firebaseUid)) {
            return;
        }

        Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();
        UserPrincipal principal = new UserPrincipal(
                user.getId(), user.getFirebaseUid(), user.getEmail(), user.getRoles());
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
