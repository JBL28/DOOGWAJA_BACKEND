package dev.ssafy.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationTime;
    private final long refreshTokenExpirationTime;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpirationTime,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpirationTime) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationTime = accessTokenExpirationTime;
        this.refreshTokenExpirationTime = refreshTokenExpirationTime;
    }

    public String createAccessToken(Long userId, String nickname, String role) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.accessTokenExpirationTime);

        return Jwts.builder()
                .subject(nickname)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId, String nickname) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.refreshTokenExpirationTime);

        return Jwts.builder()
                .subject(nickname)
                .claim("userId", userId)
                .issuedAt(new Date(now))
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get("role") == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("role").toString().split(","))
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.get("userId").toString(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, accessToken, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(accessToken).getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }
}
