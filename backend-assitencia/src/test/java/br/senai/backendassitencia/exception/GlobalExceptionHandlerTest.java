package br.senai.backendassitencia.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setup() {
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/teste");
    }

    @Test
    @DisplayName("handleResourceNotFoundException - Retorna 404 NOT_FOUND")
    void deveTratarResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Registro inexistente");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource Not Found", response.getBody().error());
        assertEquals("Registro inexistente", response.getBody().message());
    }

    @Test
    @DisplayName("handleBusinessException - Retorna 400 BAD_REQUEST")
    void deveTratarBusinessException() {
        BusinessException ex = new BusinessException("Regra de negócio violada");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Regra de negócio violada", response.getBody().message());
    }

    @Test
    @DisplayName("handleInvalidTokenException - Retorna 401 UNAUTHORIZED")
    void deveTratarInvalidTokenException() {
        InvalidTokenException ex = new InvalidTokenException("Token expirado");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidTokenException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().error());
        assertEquals("Token expirado", response.getBody().message());
    }

    @Test
    @DisplayName("handleBadCredentialsException - Retorna 401 UNAUTHORIZED")
    void deveTratarBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Falha de autenticação");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentialsException(ex, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Usuário ou senha inválidos.", response.getBody().message());
    }

    @Test
    @DisplayName("handleAccessDeniedException - Retorna 403 FORBIDDEN")
    void deveTratarAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Sem permissão");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Você não tem permissão para acessar este recurso.", response.getBody().message());
    }

    @Test
    @DisplayName("handleMethodArgumentNotValidException - Retorna 400 com lista de erros de campo")
    void deveTratarMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("usuario", "email", "Email inválido");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValidException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().errors());
        assertEquals(1, response.getBody().errors().size());
        assertEquals("email", response.getBody().errors().get(0).field());
        assertEquals("Email inválido", response.getBody().errors().get(0).message());
    }

    @Test
    @DisplayName("handleConstraintViolationException - Retorna 400 com lista de violações")
    void deveTratarConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("id");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("não deve ser nulo");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody().errors());
        assertEquals(1, response.getBody().errors().size());
        assertEquals("id", response.getBody().errors().get(0).field());
    }

    @Test
    @DisplayName("handleHttpMessageNotReadableException - Retorna 400 BAD_REQUEST")
    void deveTratarHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHttpMessageNotReadableException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Corpo da requisição malformado ou inválido.", response.getBody().message());
    }

    @Test
    @DisplayName("handleGeneralException - Retorna 500 INTERNAL_SERVER_ERROR")
    void deveTratarGeneralException() {
        RuntimeException ex = new RuntimeException("Falha inesperada");
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneralException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Ocorreu um erro interno inesperado no servidor.", response.getBody().message());
    }
}
