package com.example.digigoods.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserDetailsServiceImpl userDetailsService;

  private User testUser;
  private String testUsername;
  private String testPassword;

  @BeforeEach
  void setUp() {
    testUsername = "testuser";
    testPassword = "encodedPassword123";

    testUser = new User();
    testUser.setId(1L);
    testUser.setUsername(testUsername);
    testUser.setPassword(testPassword);
  }

  @Nested
  @DisplayName("Successful User Loading")
  class SuccessfulUserLoading {

    @Test
    @DisplayName("Given existing username, when loading user, then return UserDetails with correct username")
    void givenExistingUsername_whenLoadingUser_thenReturnUserDetailsWithCorrectUsername() {
      // Arrange
      when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

      // Act
      UserDetails userDetails = userDetailsService.loadUserByUsername(testUsername);

      // Assert
      assertNotNull(userDetails);
      assertEquals(testUsername, userDetails.getUsername());
      verify(userRepository).findByUsername(testUsername);
    }

    @Test
    @DisplayName("Given existing username, when loading user, then return UserDetails with correct password")
    void givenExistingUsername_whenLoadingUser_thenReturnUserDetailsWithCorrectPassword() {
      // Arrange
      when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

      // Act
      UserDetails userDetails = userDetailsService.loadUserByUsername(testUsername);

      // Assert
      assertNotNull(userDetails);
      assertEquals(testPassword, userDetails.getPassword());
      verify(userRepository).findByUsername(testUsername);
    }

    @Test
    @DisplayName("Given existing username, when loading user, then return UserDetails with empty authorities")
    void givenExistingUsername_whenLoadingUser_thenReturnUserDetailsWithEmptyAuthorities() {
      // Arrange
      when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

      // Act
      UserDetails userDetails = userDetailsService.loadUserByUsername(testUsername);

      // Assert
      assertNotNull(userDetails);
      assertNotNull(userDetails.getAuthorities());
      assertTrue(userDetails.getAuthorities().isEmpty());
      verify(userRepository).findByUsername(testUsername);
    }

    @Test
    @DisplayName("Given existing username, when loading user, then return enabled UserDetails")
    void givenExistingUsername_whenLoadingUser_thenReturnEnabledUserDetails() {
      // Arrange
      when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

      // Act
      UserDetails userDetails = userDetailsService.loadUserByUsername(testUsername);

      // Assert
      assertNotNull(userDetails);
      assertTrue(userDetails.isEnabled());
      assertTrue(userDetails.isAccountNonExpired());
      assertTrue(userDetails.isAccountNonLocked());
      assertTrue(userDetails.isCredentialsNonExpired());
      verify(userRepository).findByUsername(testUsername);
    }

    @Test
    @DisplayName("Given different existing user, when loading user, then return correct UserDetails")
    void givenDifferentExistingUser_whenLoadingUser_thenReturnCorrectUserDetails() {
      // Arrange
      String differentUsername = "anotheruser";
      String differentPassword = "anotherPassword456";

      User differentUser = new User();
      differentUser.setId(2L);
      differentUser.setUsername(differentUsername);
      differentUser.setPassword(differentPassword);

      when(userRepository.findByUsername(differentUsername)).thenReturn(Optional.of(differentUser));

      // Act
      UserDetails userDetails = userDetailsService.loadUserByUsername(differentUsername);

      // Assert
      assertNotNull(userDetails);
      assertEquals(differentUsername, userDetails.getUsername());
      assertEquals(differentPassword, userDetails.getPassword());
      verify(userRepository).findByUsername(differentUsername);
    }
  }

  @Nested
  @DisplayName("User Not Found Scenarios")
  class UserNotFoundScenarios {

    @Test
    @DisplayName("Given non-existent username, when loading user, then throw UsernameNotFoundException")
    void givenNonExistentUsername_whenLoadingUser_thenThrowUsernameNotFoundException() {
      // Arrange
      String nonExistentUsername = "nonexistentuser";
      when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

      // Act & Assert
      UsernameNotFoundException exception = assertThrows(
          UsernameNotFoundException.class,
          () -> userDetailsService.loadUserByUsername(nonExistentUsername));

      assertEquals("User not found: " + nonExistentUsername, exception.getMessage());
      verify(userRepository).findByUsername(nonExistentUsername);
    }

    @Test
    @DisplayName("Given null username, when loading user, then throw UsernameNotFoundException")
    void givenNullUsername_whenLoadingUser_thenThrowUsernameNotFoundException() {
      // Arrange
      String nullUsername = null;
      when(userRepository.findByUsername(nullUsername)).thenReturn(Optional.empty());

      // Act & Assert
      UsernameNotFoundException exception = assertThrows(
          UsernameNotFoundException.class,
          () -> userDetailsService.loadUserByUsername(nullUsername));

      assertEquals("User not found: null", exception.getMessage());
      verify(userRepository).findByUsername(nullUsername);
    }

    @Test
    @DisplayName("Given empty username, when loading user, then throw UsernameNotFoundException")
    void givenEmptyUsername_whenLoadingUser_thenThrowUsernameNotFoundException() {
      // Arrange
      String emptyUsername = "";
      when(userRepository.findByUsername(emptyUsername)).thenReturn(Optional.empty());

      // Act & Assert
      UsernameNotFoundException exception = assertThrows(
          UsernameNotFoundException.class,
          () -> userDetailsService.loadUserByUsername(emptyUsername));

      assertEquals("User not found: ", exception.getMessage());
      verify(userRepository).findByUsername(emptyUsername);
    }

    @Test
    @DisplayName("Given whitespace username, when loading user, then throw UsernameNotFoundException")
    void givenWhitespaceUsername_whenLoadingUser_thenThrowUsernameNotFoundException() {
      // Arrange
      String whitespaceUsername = "   ";
      when(userRepository.findByUsername(whitespaceUsername)).thenReturn(Optional.empty());

      // Act & Assert
      UsernameNotFoundException exception = assertThrows(
          UsernameNotFoundException.class,
          () -> userDetailsService.loadUserByUsername(whitespaceUsername));

      assertEquals("User not found: " + whitespaceUsername, exception.getMessage());
      verify(userRepository).findByUsername(whitespaceUsername);
    }
  }

  @Nested
  @DisplayName("UserDetails Object Validation")
  class UserDetailsObjectValidation {

    @Test
    @DisplayName("Given existing user, when loading user, then return Spring Security User instance")
    void givenExistingUser_whenLoadingUser_thenReturnSpringSecurityUserInstance() {
      // Arrange
      when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

      // Act
      UserDetails userDetails = userDetailsService.loadUserByUsername(testUsername);

      // Assert
      assertNotNull(userDetails);
      assertTrue(userDetails instanceof org.springframework.security.core.userdetails.User);
      verify(userRepository).findByUsername(testUsername);
    }

    @Test
    @DisplayName("Given user with special characters in username, when loading user, then handle correctly")
    void givenUserWithSpecialCharactersInUsername_whenLoadingUser_thenHandleCorrectly() {
      // Arrange
      String specialUsername = "user@example.com";
      User specialUser = new User();
      specialUser.setId(3L);
      specialUser.setUsername(specialUsername);
      specialUser.setPassword("password123");

      when(userRepository.findByUsername(specialUsername)).thenReturn(Optional.of(specialUser));

      // Act
      UserDetails userDetails = userDetailsService.loadUserByUsername(specialUsername);

      // Assert
      assertNotNull(userDetails);
      assertEquals(specialUsername, userDetails.getUsername());
      assertEquals("password123", userDetails.getPassword());
      verify(userRepository).findByUsername(specialUsername);
    }

    @Test
    @DisplayName("Given user with long username, when loading user, then handle correctly")
    void givenUserWithLongUsername_whenLoadingUser_thenHandleCorrectly() {
      // Arrange
      String longUsername = "verylongusernamethatexceedsnormallengthbutshouldbehanded";
      User longUsernameUser = new User();
      longUsernameUser.setId(4L);
      longUsernameUser.setUsername(longUsername);
      longUsernameUser.setPassword("password123");

      when(userRepository.findByUsername(longUsername)).thenReturn(Optional.of(longUsernameUser));

      // Act
      UserDetails userDetails = userDetailsService.loadUserByUsername(longUsername);

      // Assert
      assertNotNull(userDetails);
      assertEquals(longUsername, userDetails.getUsername());
      assertEquals("password123", userDetails.getPassword());
      verify(userRepository).findByUsername(longUsername);
    }

    @Test
    @DisplayName("Given user with null password, when loading user, then throw IllegalArgumentException")
    void givenUserWithNullPassword_whenLoadingUser_thenThrowIllegalArgumentException() {
      // Arrange
      User userWithNullPassword = new User();
      userWithNullPassword.setId(5L);
      userWithNullPassword.setUsername(testUsername);
      userWithNullPassword.setPassword(null);

      when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(userWithNullPassword));

      // Act & Assert
      IllegalArgumentException exception = assertThrows(
          IllegalArgumentException.class,
          () -> userDetailsService.loadUserByUsername(testUsername));

      assertTrue(exception.getMessage().contains("Cannot pass null or empty values to constructor"));
      verify(userRepository).findByUsername(testUsername);
    }
  }

  @Nested
  @DisplayName("Repository Integration")
  class RepositoryIntegration {

    @Test
    @DisplayName("Given username, when loading user, then call repository exactly once")
    void givenUsername_whenLoadingUser_thenCallRepositoryExactlyOnce() {
      // Arrange
      when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

      // Act
      userDetailsService.loadUserByUsername(testUsername);

      // Assert
      verify(userRepository).findByUsername(testUsername);
    }

    @Test
    @DisplayName("Given case-sensitive username, when loading user, then pass exact username to repository")
    void givenCaseSensitiveUsername_whenLoadingUser_thenPassExactUsernameToRepository() {
      // Arrange
      String caseSensitiveUsername = "TestUser";
      User caseSensitiveUser = new User();
      caseSensitiveUser.setId(6L);
      caseSensitiveUser.setUsername(caseSensitiveUsername);
      caseSensitiveUser.setPassword("password123");

      when(userRepository.findByUsername(caseSensitiveUsername)).thenReturn(Optional.of(caseSensitiveUser));

      // Act
      UserDetails userDetails = userDetailsService.loadUserByUsername(caseSensitiveUsername);

      // Assert
      assertNotNull(userDetails);
      assertEquals(caseSensitiveUsername, userDetails.getUsername());
      verify(userRepository).findByUsername(caseSensitiveUsername);
    }
  }
}
