package com.example.digigoods.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.digigoods.dto.LoginRequest;
import com.example.digigoods.dto.LoginResponse;
import com.example.digigoods.model.User;
import com.example.digigoods.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtService jwtService;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private AuthService authService;

  private LoginRequest validLoginRequest;
  private User testUser;

  @BeforeEach
  void setUp() {
    validLoginRequest = new LoginRequest();
    validLoginRequest.setUsername("testuser");
    validLoginRequest.setPassword("password123");

    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername("testuser");
    testUser.setPassword("encodedPassword");
  }

  @Nested
  @DisplayName("Login Tests")
  class LoginTests {

    @Test
    @DisplayName("Given valid credentials, when logging in, then return login response with JWT token")
    void givenValidCredentials_whenLoggingIn_thenReturnLoginResponseWithJwtToken() {
      // Arrange
      String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
      
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(jwtService.generateToken(testUser.getId(), testUser.getUsername())).thenReturn(expectedToken);

      // Act
      LoginResponse response = authService.login(validLoginRequest);

      // Assert
      assertNotNull(response);
      assertEquals(expectedToken, response.getToken());
      assertEquals(testUser.getId(), response.getUserId());
      assertEquals(testUser.getUsername(), response.getUsername());

      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(userRepository).findByUsername("testuser");
      verify(jwtService).generateToken(testUser.getId(), testUser.getUsername());
    }

    @Test
    @DisplayName("Given valid credentials, when logging in, then authenticate with correct credentials")
    void givenValidCredentials_whenLoggingIn_thenAuthenticateWithCorrectCredentials() {
      // Arrange
      String expectedToken = "jwt.token.here";
      
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
      when(jwtService.generateToken(testUser.getId(), testUser.getUsername())).thenReturn(expectedToken);

      // Act
      authService.login(validLoginRequest);

      // Assert
      verify(authenticationManager).authenticate(
          eq(new UsernamePasswordAuthenticationToken("testuser", "password123"))
      );
    }

    @Test
    @DisplayName("Given invalid credentials, when logging in, then throw BadCredentialsException")
    void givenInvalidCredentials_whenLoggingIn_thenThrowBadCredentialsException() {
      // Arrange
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
          .thenThrow(new BadCredentialsException("Bad credentials"));

      // Act & Assert
      assertThrows(BadCredentialsException.class, () -> authService.login(validLoginRequest));
      
      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Given authentication succeeds but user not found in database, when logging in, then throw RuntimeException")
    void givenAuthenticationSucceedsButUserNotFoundInDatabase_whenLoggingIn_thenThrowRuntimeException() {
      // Arrange
      when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

      // Act & Assert
      RuntimeException exception = assertThrows(RuntimeException.class, 
          () -> authService.login(validLoginRequest));
      assertEquals("User not found", exception.getMessage());

      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
      verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Given authentication manager throws AuthenticationException, when logging in, then propagate exception")
    void givenAuthenticationManagerThrowsAuthenticationException_whenLoggingIn_thenPropagateException() {
      // Arrange
      AuthenticationException authException = new BadCredentialsException("Invalid password");
      when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
          .thenThrow(authException);

      // Act & Assert
      AuthenticationException thrownException = assertThrows(AuthenticationException.class,
          () -> authService.login(validLoginRequest));
      assertEquals("Invalid password", thrownException.getMessage());

      verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Given different username, when logging in, then use correct username for authentication and user lookup")
    void givenDifferentUsername_whenLoggingIn_thenUseCorrectUsernameForAuthenticationAndUserLookup() {
      // Arrange
      LoginRequest differentLoginRequest = new LoginRequest();
      differentLoginRequest.setUsername("anotheruser");
      differentLoginRequest.setPassword("anotherpassword");

      User anotherUser = new User();
      anotherUser.setId(2L);
      anotherUser.setUsername("anotheruser");
      anotherUser.setPassword("encodedAnotherPassword");

      String expectedToken = "another.jwt.token";

      when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(anotherUser));
      when(jwtService.generateToken(anotherUser.getId(), anotherUser.getUsername())).thenReturn(expectedToken);

      // Act
      LoginResponse response = authService.login(differentLoginRequest);

      // Assert
      assertNotNull(response);
      assertEquals(expectedToken, response.getToken());
      assertEquals(anotherUser.getId(), response.getUserId());
      assertEquals(anotherUser.getUsername(), response.getUsername());

      verify(authenticationManager).authenticate(
          eq(new UsernamePasswordAuthenticationToken("anotheruser", "anotherpassword"))
      );
      verify(userRepository).findByUsername("anotheruser");
      verify(jwtService).generateToken(anotherUser.getId(), anotherUser.getUsername());
    }

    @Test
    @DisplayName("Given user with different ID, when logging in, then generate token with correct user ID")
    void givenUserWithDifferentId_whenLoggingIn_thenGenerateTokenWithCorrectUserId() {
      // Arrange
      User userWithDifferentId = new User();
      userWithDifferentId.setId(999L);
      userWithDifferentId.setUsername("testuser");
      userWithDifferentId.setPassword("encodedPassword");

      String expectedToken = "token.with.different.id";

      when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userWithDifferentId));
      when(jwtService.generateToken(999L, "testuser")).thenReturn(expectedToken);

      // Act
      LoginResponse response = authService.login(validLoginRequest);

      // Assert
      assertNotNull(response);
      assertEquals(expectedToken, response.getToken());
      assertEquals(999L, response.getUserId());
      assertEquals("testuser", response.getUsername());

      verify(jwtService).generateToken(999L, "testuser");
    }
  }
}
