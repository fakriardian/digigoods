# Security Configuration Guide

## Overview

This document outlines the security measures implemented in the Digigoods API and provides guidance for secure deployment.

## Security Measures Implemented

### 1. Environment Variable Configuration

**Database Configuration:**
```bash
# Production environment variables
export DB_URL="jdbc:postgresql://prod-host:5432/digigoods"
export DB_USERNAME="prod-user"
export DB_PASSWORD="secure-production-password"
```

**JWT Configuration:**
```bash
# Production environment variables
export JWT_SECRET="your-very-secure-256-bit-secret-key-for-production"
export JWT_EXPIRATION="3600000"  # 1 hour in milliseconds
```

### 2. Security Features

- **JWT Authentication**: Stateless authentication using JSON Web Tokens
- **Password Encryption**: BCrypt password hashing with salt
- **CSRF Protection**: Disabled for stateless API (appropriate for JWT-based APIs)
- **Session Management**: Stateless session policy
- **Input Validation**: Request validation using Spring Boot validation
- **Exception Handling**: Secure error responses without sensitive information exposure

### 3. Security Headers

The application should be deployed behind a reverse proxy (nginx/Apache) with security headers:

```nginx
# Security headers
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-Content-Type-Options "nosniff" always;
add_header X-XSS-Protection "1; mode=block" always;
add_header Referrer-Policy "strict-origin-when-cross-origin" always;
add_header Content-Security-Policy "default-src 'self'" always;
```

### 4. Database Security

- Use connection pooling with proper timeout settings
- Enable SSL/TLS for database connections in production
- Use database user with minimal required privileges
- Regular database security updates

### 5. Deployment Security Checklist

- [ ] Set strong, unique passwords for all accounts
- [ ] Use environment variables for all sensitive configuration
- [ ] Enable HTTPS/TLS for all communications
- [ ] Configure proper firewall rules
- [ ] Regular security updates for all dependencies
- [ ] Monitor and log security events
- [ ] Use secrets management system (e.g., HashiCorp Vault, AWS Secrets Manager)

## Production Deployment

### Environment Variables Template

Create a `.env` file (never commit to version control):

```bash
# Database Configuration
DB_URL=jdbc:postgresql://your-db-host:5432/digigoods
DB_USERNAME=your-db-user
DB_PASSWORD=your-secure-db-password

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-minimum-32-characters
JWT_EXPIRATION=3600000

# Application Configuration
SPRING_PROFILES_ACTIVE=production
```

### Docker Deployment

```dockerfile
# Use environment variables in Docker
ENV DB_URL=${DB_URL}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}
ENV JWT_SECRET=${JWT_SECRET}
ENV JWT_EXPIRATION=${JWT_EXPIRATION}
```

## Security Monitoring

### Recommended Monitoring

1. **Failed Authentication Attempts**: Monitor and alert on excessive failed logins
2. **Unusual Access Patterns**: Monitor for suspicious API usage patterns
3. **Error Rates**: Monitor application error rates for potential attacks
4. **Database Connections**: Monitor database connection patterns

### Logging

The application logs security-relevant events. Ensure logs are:
- Stored securely
- Regularly rotated
- Monitored for security events
- Never contain sensitive information (passwords, tokens)

## Security Updates

- Regularly update all dependencies using `mvn versions:display-dependency-updates`
- Monitor security advisories for Spring Boot and other dependencies
- Use tools like OWASP Dependency Check for vulnerability scanning

## Contact

For security issues, please contact the development team immediately.
