package com.example.DocsSignatureAppBE.Interceptor;

import com.example.DocsSignatureAppBE.Service.AuditService;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class AuditInterceptor implements HandlerInterceptor {

    private final AuditService auditService;

    public AuditInterceptor(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String username = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "Anonymous";
        String method = request.getMethod();
        String uri = request.getRequestURI();
        auditService.logAction(username, method + " " + uri);
        return true; // Continue with the request
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        String ipAddress = getClientIp(request);
        String action = extractAction(request.getRequestURI());
        auditService.logAction(action, ipAddress, String.valueOf(request));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private String extractAction(String uri) {
        if (uri.contains("upload")) return "upload";
        if (uri.contains("view")) return "view";
        if (uri.contains("sign")) return "sign";
        return "unknown";
    }

}
