package com.fluxmall.global.resolver;

import com.fluxmall.global.annotation.CurrentMemberId;
import com.fluxmall.global.exception.BusinessException;
import com.fluxmall.auth.exception.AuthError;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @CurrentMemberId 어노테이션을 처리하는 ArgumentResolver
 * SecurityContext에서 인증된 사용자의 memberId를 추출하여 컨트롤러 파라미터로 주입합니다.
 *
 * 사용 예시:
 * <pre>
 * @GetMapping("/profile")
 * public SingleResult<MemberResponse> getProfile(@CurrentMemberId Long memberId) {
 *     // memberId가 자동으로 주입됨
 * }
 * </pre>
 */
@Component
public class CurrentMemberIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMemberId.class)
               && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나 익명 사용자인 경우
        if (authentication == null || !authentication.isAuthenticated()
            || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(AuthError.UNAUTHORIZED);
        }

        // JwtAuthenticationFilter에서 principal에 memberId(Long)를 직접 저장
        Object principal = authentication.getPrincipal();

        if (principal instanceof Long) {
            return principal;
        }

        throw new BusinessException(AuthError.INVALID_TOKEN);
    }
}
