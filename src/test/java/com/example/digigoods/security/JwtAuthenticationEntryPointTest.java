package com.example.digigoods.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationEntryPoint Tests")
class JwtAuthenticationEntryPointTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @InjectMocks
  private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  private StringWriter stringWriter;
  private PrintWriter printWriter;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws IOException {
    stringWriter = new StringWriter();
    printWriter = new PrintWriter(stringWriter);
    objectMapper = new ObjectMapper();
    
    when(response.getWriter()).thenReturn(printWriter);
  }

  private void assertErrorResponse(String responseBody, String expectedPath) throws IOException {
    JsonNode jsonNode = objectMapper.readTree(responseBody);
    assertEquals(401, jsonNode.get("status").asInt());
    assertEquals("Unauthorized", jsonNode.get("error").asText());
    assertEquals("JWT token is missing or invalid", jsonNode.get("message").asText());
    assertEquals(expectedPath, jsonNode.get("path").asText());
    assertTrue(jsonNode.has("timestamp"));
  }

  @Nested
  @DisplayName("Authentication Exception Handling")
  class AuthenticationExceptionHandling {

    @Test
    @DisplayName("Given BadCredentialsException, when commence called, then return 401 with proper error response")
    void givenBadCredentialsException_whenCommenceCalled_thenReturn401WithProperErrorResponse() 
        throws IOException, ServletException {
      // Arrange
      String requestUri = "/api/orders";
      AuthenticationException authException = new BadCredentialsException("Bad credentials");
      
      when(request.getRequestURI()).thenReturn(requestUri);

      // Act
      jwtAuthenticationEntryPoint.commence(request, response, authException);

      // Assert
      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
      
      printWriter.flush();
      String responseBody = stringWriter.toString();
      assertErrorResponse(responseBody, requestUri);
    }

    @Test
    @DisplayName("Given InsufficientAuthenticationException, when commence called, then return 401 with proper error response")
    void givenInsufficientAuthenticationException_whenCommenceCalled_thenReturn401WithProperErrorResponse() 
        throws IOException, ServletException {
      // Arrange
      String requestUri = "/api/checkout";
      AuthenticationException authException = new InsufficientAuthenticationException("Insufficient authentication");
      
      when(request.getRequestURI()).thenReturn(requestUri);

      // Act
      jwtAuthenticationEntryPoint.commence(request, response, authException);

      // Assert
      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
      
      printWriter.flush();
      String responseBody = stringWriter.toString();
      assertErrorResponse(responseBody, requestUri);
    }

    @Test
    @DisplayName("Given SessionAuthenticationException, when commence called, then return 401 with proper error response")
    void givenSessionAuthenticationException_whenCommenceCalled_thenReturn401WithProperErrorResponse() 
        throws IOException, ServletException {
      // Arrange
      String requestUri = "/api/protected";
      AuthenticationException authException = new SessionAuthenticationException("Session authentication failed");
      
      when(request.getRequestURI()).thenReturn(requestUri);

      // Act
      jwtAuthenticationEntryPoint.commence(request, response, authException);

      // Assert
      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
      
      printWriter.flush();
      String responseBody = stringWriter.toString();
      assertErrorResponse(responseBody, requestUri);
    }

    @Test
    @DisplayName("Given generic AuthenticationException, when commence called, then return 401 with proper error response")
    void givenGenericAuthenticationException_whenCommenceCalled_thenReturn401WithProperErrorResponse() 
        throws IOException, ServletException {
      // Arrange
      String requestUri = "/api/secure";
      AuthenticationException authException = new AuthenticationException("Generic authentication error") {};
      
      when(request.getRequestURI()).thenReturn(requestUri);

      // Act
      jwtAuthenticationEntryPoint.commence(request, response, authException);

      // Assert
      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
      
      printWriter.flush();
      String responseBody = stringWriter.toString();
      assertErrorResponse(responseBody, requestUri);
    }
  }

  @Nested
  @DisplayName("HTTP Response Configuration")
  class HttpResponseConfiguration {

    @Test
    @DisplayName("Given authentication exception, when commence called, then set correct HTTP status")
    void givenAuthenticationException_whenCommenceCalled_thenSetCorrectHttpStatus() 
        throws IOException, ServletException {
      // Arrange
      AuthenticationException authException = new BadCredentialsException("Bad credentials");
      when(request.getRequestURI()).thenReturn("/api/test");

      // Act
      jwtAuthenticationEntryPoint.commence(request, response, authException);

      // Assert
      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(response).setStatus(401);
    }

    @Test
    @DisplayName("Given authentication exception, when commence called, then set correct content type")
    void givenAuthenticationException_whenCommenceCalled_thenSetCorrectContentType() 
        throws IOException, ServletException {
      // Arrange
      AuthenticationException authException = new BadCredentialsException("Bad credentials");
      when(request.getRequestURI()).thenReturn("/api/test");

      // Act
      jwtAuthenticationEntryPoint.commence(request, response, authException);

      // Assert
      verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
      verify(response).setContentType("application/json");
    }

    @Test
    @DisplayName("Given authentication exception, when commence called, then write response to output")
    void givenAuthenticationException_whenCommenceCalled_thenWriteResponseToOutput() 
        throws IOException, ServletException {
      // Arrange
      AuthenticationException authException = new BadCredentialsException("Bad credentials");
      when(request.getRequestURI()).thenReturn("/api/test");

      // Act
      jwtAuthenticationEntryPoint.commence(request, response, authException);

      // Assert
      verify(response).getWriter();
      
      printWriter.flush();
      String responseBody = stringWriter.toString();
      assertTrue(responseBody.contains("\"status\":401"));
      assertTrue(responseBody.contains("\"error\":\"Unauthorized\""));
      assertTrue(responseBody.contains("\"message\":\"JWT token is missing or invalid\""));
    }
  }

  @Nested
  @DisplayName("Error Response Content Validation")
  class ErrorResponseContentValidation {

    @Test
    @DisplayName("Given different request URIs, when commence called, then include correct path in response")
    void givenDifferentRequestUris_whenCommenceCalled_thenIncludeCorrectPathInResponse() 
        throws IOException, ServletException {
      // Arrange
      String[] testUris = {"/api/orders", "/api/products", "/api/discounts", "/secure/admin"};
      
      for (String uri : testUris) {
        // Reset the StringWriter for each test
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        
        AuthenticationException authException = new BadCredentialsException("Bad credentials");
        when(request.getRequestURI()).thenReturn(uri);

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Assert
        printWriter.flush();
        String responseBody = stringWriter.toString();
        assertTrue(responseBody.contains("\"path\":\"" + uri + "\""));
      }
    }

    @Test
    @DisplayName("Given null request URI, when commence called, then handle gracefully")
    void givenNullRequestUri_whenCommenceCalled_thenHandleGracefully() 
        throws IOException, ServletException {
      // Arrange
      AuthenticationException authException = new BadCredentialsException("Bad credentials");
      when(request.getRequestURI()).thenReturn(null);

      // Act
      jwtAuthenticationEntryPoint.commence(request, response, authException);

      // Assert
      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
      
      printWriter.flush();
      String responseBody = stringWriter.toString();
      assertTrue(responseBody.contains("\"path\":null"));
    }

    @Test
    @DisplayName("Given empty request URI, when commence called, then handle gracefully")
    void givenEmptyRequestUri_whenCommenceCalled_thenHandleGracefully() 
        throws IOException, ServletException {
      // Arrange
      AuthenticationException authException = new BadCredentialsException("Bad credentials");
      when(request.getRequestURI()).thenReturn("");

      // Act
      jwtAuthenticationEntryPoint.commence(request, response, authException);

      // Assert
      verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
      verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
      
      printWriter.flush();
      String responseBody = stringWriter.toString();
      assertTrue(responseBody.contains("\"path\":\"\""));
    }

    @Test
    @DisplayName("Given authentication exception, when commence called, then response contains all required fields")
    void givenAuthenticationException_whenCommenceCalled_thenResponseContainsAllRequiredFields() 
        throws IOException, ServletException {
      // Arrange
      String requestUri = "/api/test";
      AuthenticationException authException = new BadCredentialsException("Bad credentials");
      when(request.getRequestURI()).thenReturn(requestUri);

      // Act
      jwtAuthenticationEntryPoint.commence(request, response, authException);

      // Assert
      printWriter.flush();
      String responseBody = stringWriter.toString();
      
      // Verify all required fields are present
      assertTrue(responseBody.contains("\"status\":401"));
      assertTrue(responseBody.contains("\"error\":\"Unauthorized\""));
      assertTrue(responseBody.contains("\"message\":\"JWT token is missing or invalid\""));
      assertTrue(responseBody.contains("\"path\":\"" + requestUri + "\""));
      assertTrue(responseBody.contains("\"timestamp\""));
    }
  }
}
