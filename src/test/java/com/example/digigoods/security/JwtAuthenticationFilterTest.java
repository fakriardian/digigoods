package com.example.digigoods.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.digigoods.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  private JwtService jwtService;

  @Mock
  private UserDetailsService userDetailsService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private UserDetails userDetails;
  private String validToken;
  private String validUsername;

  @BeforeEach
  void setUp() {
    validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    validUsername = "testuser";

    userDetails = new User(validUsername, "password", new ArrayList<>());

    SecurityContextHolder.setContext(securityContext);
  }

  @Nested
  @DisplayName("Valid JWT Token Processing")
  class ValidJwtTokenProcessing {

    @Test
    @DisplayName("Given valid Bearer token, when filtering, then authenticate user and continue chain")
    void givenValidBearerToken_whenFiltering_thenAuthenticateUserAndContinueChain()
        throws ServletException, IOException {
      // Arrange
      String authHeader = "Bearer " + validToken;

      when(request.getHeader("Authorization")).thenReturn(authHeader);
      when(jwtService.extractUsername(validToken)).thenReturn(validUsername);
      when(securityContext.getAuthentication()).thenReturn(null);
      when(userDetailsService.loadUserByUsername(validUsername)).thenReturn(userDetails);
      when(jwtService.validateToken(validToken, validUsername)).thenReturn(true);

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(jwtService).extractUsername(validToken);
      verify(userDetailsService).loadUserByUsername(validUsername);
      verify(jwtService).validateToken(validToken, validUsername);
      verify(securityContext).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Given valid token with existing authentication, when filtering, then skip authentication")
    void givenValidTokenWithExistingAuthentication_whenFiltering_thenSkipAuthentication()
        throws ServletException, IOException {
      // Arrange
      String authHeader = "Bearer " + validToken;
      UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken("user", null,
          new ArrayList<>());

      when(request.getHeader("Authorization")).thenReturn(authHeader);
      when(jwtService.extractUsername(validToken)).thenReturn(validUsername);
      when(securityContext.getAuthentication()).thenReturn(existingAuth);

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(jwtService).extractUsername(validToken);
      verify(userDetailsService, never()).loadUserByUsername(anyString());
      verify(jwtService, never()).validateToken(anyString(), anyString());
      verify(securityContext, never()).setAuthentication(any());
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Given valid token but invalid validation, when filtering, then skip authentication")
    void givenValidTokenButInvalidValidation_whenFiltering_thenSkipAuthentication()
        throws ServletException, IOException {
      // Arrange
      String authHeader = "Bearer " + validToken;

      when(request.getHeader("Authorization")).thenReturn(authHeader);
      when(jwtService.extractUsername(validToken)).thenReturn(validUsername);
      when(securityContext.getAuthentication()).thenReturn(null);
      when(userDetailsService.loadUserByUsername(validUsername)).thenReturn(userDetails);
      when(jwtService.validateToken(validToken, validUsername)).thenReturn(false);

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(jwtService).extractUsername(validToken);
      verify(userDetailsService).loadUserByUsername(validUsername);
      verify(jwtService).validateToken(validToken, validUsername);
      verify(securityContext, never()).setAuthentication(any());
      verify(filterChain).doFilter(request, response);
    }
  }

  @Nested
  @DisplayName("Missing or Invalid Authorization Header")
  class MissingOrInvalidAuthorizationHeader {

    @Test
    @DisplayName("Given missing Authorization header, when filtering, then skip authentication")
    void givenMissingAuthorizationHeader_whenFiltering_thenSkipAuthentication()
        throws ServletException, IOException {
      // Arrange
      when(request.getHeader("Authorization")).thenReturn(null);

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(jwtService, never()).extractUsername(anyString());
      verify(userDetailsService, never()).loadUserByUsername(anyString());
      verify(jwtService, never()).validateToken(anyString(), anyString());
      verify(securityContext, never()).setAuthentication(any());
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Given Authorization header without Bearer prefix, when filtering, then skip authentication")
    void givenAuthorizationHeaderWithoutBearerPrefix_whenFiltering_thenSkipAuthentication()
        throws ServletException, IOException {
      // Arrange
      when(request.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0");

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(jwtService, never()).extractUsername(anyString());
      verify(userDetailsService, never()).loadUserByUsername(anyString());
      verify(jwtService, never()).validateToken(anyString(), anyString());
      verify(securityContext, never()).setAuthentication(any());
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Given Bearer token with only prefix, when filtering, then skip authentication")
    void givenBearerTokenWithOnlyPrefix_whenFiltering_thenSkipAuthentication()
        throws ServletException, IOException {
      // Arrange
      when(request.getHeader("Authorization")).thenReturn("Bearer ");

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(jwtService).extractUsername("");
      verify(filterChain).doFilter(request, response);
    }
  }

  @Nested
  @DisplayName("JWT Token Exception Handling")
  class JwtTokenExceptionHandling {

    @Test
    @DisplayName("Given expired JWT token, when filtering, then handle exception and continue")
    void givenExpiredJwtToken_whenFiltering_thenHandleExceptionAndContinue()
        throws ServletException, IOException {
      // Arrange
      String authHeader = "Bearer " + validToken;

      when(request.getHeader("Authorization")).thenReturn(authHeader);
      when(jwtService.extractUsername(validToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(jwtService).extractUsername(validToken);
      verify(userDetailsService, never()).loadUserByUsername(anyString());
      verify(jwtService, never()).validateToken(anyString(), anyString());
      verify(securityContext, never()).setAuthentication(any());
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Given malformed JWT token, when filtering, then handle exception and continue")
    void givenMalformedJwtToken_whenFiltering_thenHandleExceptionAndContinue()
        throws ServletException, IOException {
      // Arrange
      String authHeader = "Bearer " + validToken;

      when(request.getHeader("Authorization")).thenReturn(authHeader);
      when(jwtService.extractUsername(validToken)).thenThrow(new MalformedJwtException("Malformed token"));

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(jwtService).extractUsername(validToken);
      verify(userDetailsService, never()).loadUserByUsername(anyString());
      verify(jwtService, never()).validateToken(anyString(), anyString());
      verify(securityContext, never()).setAuthentication(any());
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Given JWT token with invalid signature, when filtering, then handle exception and continue")
    void givenJwtTokenWithInvalidSignature_whenFiltering_thenHandleExceptionAndContinue()
        throws ServletException, IOException {
      // Arrange
      String authHeader = "Bearer " + validToken;

      when(request.getHeader("Authorization")).thenReturn(authHeader);
      when(jwtService.extractUsername(validToken)).thenThrow(new SignatureException("Invalid signature"));

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(jwtService).extractUsername(validToken);
      verify(userDetailsService, never()).loadUserByUsername(anyString());
      verify(jwtService, never()).validateToken(anyString(), anyString());
      verify(securityContext, never()).setAuthentication(any());
      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Given JWT token with illegal argument, when filtering, then handle exception and continue")
    void givenJwtTokenWithIllegalArgument_whenFiltering_thenHandleExceptionAndContinue()
        throws ServletException, IOException {
      // Arrange
      String authHeader = "Bearer " + validToken;

      when(request.getHeader("Authorization")).thenReturn(authHeader);
      when(jwtService.extractUsername(validToken)).thenThrow(new IllegalArgumentException("Illegal argument"));

      // Act
      jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

      // Assert
      verify(jwtService).extractUsername(validToken);
      verify(userDetailsService, never()).loadUserByUsername(anyString());
      verify(jwtService, never()).validateToken(anyString(), anyString());
      verify(securityContext, never()).setAuthentication(any());
      verify(filterChain).doFilter(request, response);
    }
  }
}
