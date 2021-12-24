package com.example.answersboxapi.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.example.answersboxapi.exceptions.TokenNotValidException;
import com.example.answersboxapi.model.UserDetailsImpl;
import com.example.answersboxapi.model.auth.TokenResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Data
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_KEY = "Bearer ";
    private static final long TOKEN_VALID_PERIOD = 10 * 60 * 1000;
    private static final long REFRESH_TOKEN_PERIOD = 30 * 60 * 1000;
    private static final String ROLE_KEY = "role";

    private final UserDetailsService userDetailsService;

    private final PasswordEncoderConfig passwordEncoder;

    public TokenResponse createToken(final Authentication authentication) {
        final Date now = new Date();
        final Date tokenValidity = new Date(now.getTime() + TOKEN_VALID_PERIOD);
        final Date refreshTokenValidity = new Date(now.getTime() + REFRESH_TOKEN_PERIOD);

        final String accessToken = generateToken(authentication, now, tokenValidity);
        final String refreshToken = generateToken(authentication, now, refreshTokenValidity);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpirationDate(tokenValidity.toInstant())
                .refreshExpirationDate(refreshTokenValidity.toInstant())
                .build();
    }

    public String generateToken(final Authentication authentication, final Date now, final Date validity) {
        final UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim(ROLE_KEY, String.valueOf(userDetails.getAuthorities()))
                .sign(passwordEncoder.getAlgorithm());
    }

    public String resolveToken(final HttpServletRequest request) {
        final String bearer = request.getHeader(AUTHORIZATION_HEADER);
        if (bearer != null && bearer.startsWith(BEARER_KEY)) {
            return bearer.substring(BEARER_KEY.length());
        }
        return null;
    }

    public Authentication getAuthentication(final String token) {
        final DecodedJWT decodedJWT = validateToken(token);
        if (decodedJWT == null) {
            throw new TokenNotValidException(String.format("Token: %s isn`t valid", token));
        }
        final String subject = decodedJWT.getSubject();
        final UserDetails userDetails = this.userDetailsService.loadUserByUsername(subject);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public DecodedJWT validateToken(final String token) {
        final JWTVerifier verifier = JWT.require(passwordEncoder.getAlgorithm()).build();
        return verifier.verify(token);
    }
}
