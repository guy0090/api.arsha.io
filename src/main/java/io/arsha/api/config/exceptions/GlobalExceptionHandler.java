package io.arsha.api.config.exceptions;

import io.arsha.api.exceptions.AbstractException;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GlobalExceptionHandler {

    private final ApplicationContext applicationContext;

    @ResponseBody
    @ExceptionHandler(AbstractException.class)
    public ExceptionResponse handleCustomExceptions(AbstractException ex, HttpServletRequest req, HttpServletResponse res) {
        res.setStatus(ex.getStatus());

        var response = ExceptionResponse.fromAbstractException(ex, req);
        logExceptionDetails(req, ex, response.id);
        return response;
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ExceptionResponse handleAllExceptions(Exception ex, HttpServletRequest req, HttpServletResponse res) {
        res.setStatus(500);

        var response = ExceptionResponse.fromException(ex, req);
        logExceptionDetails(req, ex, response.id);
        return response;
    }

    @SneakyThrows
    private Optional<Method> getExceptionOriginMethod(HttpServletRequest req) {
        var reqHandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
        var handlerExecutionChain = reqHandlerMapping.getHandler(req);
        if (Objects.isNull(handlerExecutionChain)) return Optional.empty();

        HandlerMethod handlerMethod = (HandlerMethod) handlerExecutionChain.getHandler();
        return Optional.of(handlerMethod.getMethod());
    }

    @SneakyThrows
    private void logExceptionDetails(HttpServletRequest req, Throwable ex, UUID requestId) {
        var caller = getExceptionOriginMethod(req).orElse(null);

        String message;
        Logger logger;
        if (caller == null) {
            logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
            message = String.format("[%s] %s::%s(%s) request failed: %s", requestId,
                    ex.getClass().getSimpleName(), getClass().getSimpleName(), req.getRequestURI(), ex.getMessage());
        } else {
            var clazz = caller.getDeclaringClass();
            logger = LoggerFactory.getLogger(clazz);
            message = String.format("[%s] %s::%s.%s() request failed: %s", requestId,
                    ex.getClass().getSimpleName(), clazz.getSimpleName(), caller.getName(), ex.getMessage());
        }
        logger.error(message);
    }

    record ExceptionResponse(Integer status, String message, String path, String timestamp, UUID id, Integer code) {
        public static ExceptionResponse fromAbstractException(AbstractException ex, HttpServletRequest req) {
            var timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
            return new ExceptionResponse(ex.getStatus(), ex.getMessage(), req.getRequestURI(),
                    timestamp, UUID.randomUUID(), ex.getExceptionCode().getCode());
        }

        public static ExceptionResponse fromException(Exception ex, HttpServletRequest req) {
            int status;
            String errorMsg;

            switch (ex) {
                case ConstraintViolationException cvEx -> {
                    errorMsg = cvEx.getConstraintViolations().stream()
                            .map(v -> {
                                var propertyName = ((PathImpl) v.getPropertyPath()).getLeafNode().getName();
                                var providedValue = v.getInvalidValue();
                                return String.format("Invalid value '%s' for property '%s': %s",
                                        providedValue, propertyName, v.getMessage());
                            })
                            .reduce((a, b) -> a + ", " + b).orElse("Validating request parameters failed.");

                    status = 400;
                }
                case MethodArgumentTypeMismatchException mismatchEx -> {
                    errorMsg = String.format("Invalid type for argument '%s': '%s'",
                            mismatchEx.getName(), mismatchEx.getValue());
                    status = 400;
                }
                case MissingServletRequestParameterException missingEx -> {
                    errorMsg = String.format("Missing required parameter '%s'", missingEx.getParameterName());
                    status = 400;
                }
                case MethodArgumentNotValidException i1 -> {
                    errorMsg = "Invalid request body.";
                    status = 400;
                }
                case HttpMessageNotReadableException i2 -> {
                    errorMsg = "Invalid request body.";
                    status = 400;
                }
                default -> {
                    errorMsg = "An unexpected error occurred.";
                    status = 500;
                }
            }

            var timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
            return new ExceptionResponse(status, errorMsg, req.getRequestURI(),
                    timestamp, UUID.randomUUID(), 0);
        }
    }
}
