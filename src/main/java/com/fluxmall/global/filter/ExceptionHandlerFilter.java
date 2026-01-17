package com.fluxmall.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluxmall.global.response.CommonResult;
import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 필터 체인에서 발생하는 예외를 처리하는 필터
 * JwtAuthenticationFilter에서 발생한 BusinessException을 잡아 적절한 HTTP 응답으로 변환합니다.
 *
 * 처리 가능한 예외:
 * - BusinessException: ErrorCode 기반의 상세한 에러 응답 생성
 * - 기타 예외: 500 Internal Server Error 응답
 */
@Slf4j
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BusinessException e) {
            log.error("인증 필터에서 BusinessException 발생: code={}, message={}",
                e.getErrorCode().getCode(), e.getErrorCode().getMessage());
            handleBusinessException(response, e);
        } catch (Exception e) {
            log.error("인증 필터에서 예상치 못한 예외 발생", e);
            handleUnexpectedException(response, e);
        }
    }

    /**
     * BusinessException을 처리하여 ErrorCode 기반의 JSON 응답을 생성합니다.
     *
     * @param response HTTP 응답
     * @param e BusinessException
     * @throws IOException JSON 작성 실패 시
     */
    private void handleBusinessException(HttpServletResponse response, BusinessException e)
        throws IOException {
        ErrorCode errorCode = e.getErrorCode();

        // HTTP 상태 코드 결정 (인증 관련 에러는 401 Unauthorized)
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // ErrorCode를 사용한 CommonResult 응답 생성 (기존 filtered 메서드 활용)
        CommonResult errorResponse = CommonResult.filtered(e);

        // JSON 응답 작성
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * 예상치 못한 예외를 처리하여 500 Internal Server Error 응답을 생성합니다.
     *
     * @param response HTTP 응답
     * @param e 발생한 예외
     * @throws IOException JSON 작성 실패 시
     */
    private void handleUnexpectedException(HttpServletResponse response, Exception e)
        throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 일반적인 내부 서버 오류 응답 생성
        CommonResult errorResponse = new CommonResult("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.");

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}
