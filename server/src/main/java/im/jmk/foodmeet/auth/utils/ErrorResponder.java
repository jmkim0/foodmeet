package im.jmk.foodmeet.auth.utils;

import im.jmk.foodmeet.common.exception.response.ErrorInfo;
import im.jmk.foodmeet.common.response.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

public class ErrorResponder {
    public static void sendErrorResponse(
            HttpServletResponse response, HttpStatus status, ObjectMapper mapper) throws IOException {
        Response<?> errorResponse = Response.error(ErrorInfo.of(status));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status.value());
        response.getWriter().write(mapper.writerFor(Response.class).writeValueAsString(errorResponse));
    }
}