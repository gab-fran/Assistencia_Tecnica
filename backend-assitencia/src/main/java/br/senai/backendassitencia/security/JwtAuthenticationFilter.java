package br.senai.backendassitencia.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor // Padronizado com o Service
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            response.getWriter().write("""
        {
          "message": "O cabeçalho Authorization deve seguir o padrão 'Bearer <token>'"
        }
        """);

            return;
        }

        String token = header.substring(7);

        try {
            String email = jwtService.getSubject(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Aqui você valida se o token está expirado ou adulterado (ideal ter um método isValid)
                // Se o jwtService.getSubject já lançar exceção em caso de token inválido, está ótimo.

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Passamos as authorities do UserDetails, garantindo a mesma role do banco
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (RuntimeException ex) {
            // Token inválido ou expirado: limpa o contexto
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}