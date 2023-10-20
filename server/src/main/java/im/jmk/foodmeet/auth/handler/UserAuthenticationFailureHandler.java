package im.jmk.foodmeet.auth.handler;

import im.jmk.foodmeet.auth.utils.ErrorResponder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class UserAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final ObjectMapper mapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.error("# Authentication failed: {}", exception.getMessage());
        ErrorResponder.sendErrorResponse(response, HttpStatus.UNAUTHORIZED, mapper);
        // 에러 로그를 기록하거나 error response를 전송할 수 있음
    }

}
