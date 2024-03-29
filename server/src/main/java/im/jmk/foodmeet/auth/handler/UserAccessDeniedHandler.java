package im.jmk.foodmeet.auth.handler;

import im.jmk.foodmeet.auth.utils.ErrorResponder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class UserAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ErrorResponder.sendErrorResponse(response, HttpStatus.FORBIDDEN, mapper);
        log.warn("Forbidden error happened: {}", accessDeniedException.getMessage());
    }
}