package huuthang.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

public final class SessionAction_24133057 {
    private SessionAction_24133057() { }
    public static String token(HttpServletRequest request) {
        HttpSession session=request.getSession();
        synchronized(session) {
            if(session.getAttribute("videoActionCsrf")==null) session.setAttribute("videoActionCsrf",UUID.randomUUID().toString());
            return (String)session.getAttribute("videoActionCsrf");
        }
    }
    public static boolean valid(HttpServletRequest request) {
        String submitted=request.getParameter("csrfToken");
        return submitted!=null && MessageDigest.isEqual(token(request).getBytes(StandardCharsets.UTF_8),submitted.getBytes(StandardCharsets.UTF_8));
    }
}
