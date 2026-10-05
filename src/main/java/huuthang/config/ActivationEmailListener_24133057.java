package huuthang.config;

import huuthang.service.ActivationEmailSender_24133057;
import javax.servlet.*;
import java.util.concurrent.*;

public class ActivationEmailListener_24133057 implements ServletContextListener {
    private ScheduledExecutorService executor;
    @Override public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        String base = System.getenv("APP_BASE_URL");
        if (base == null || base.isBlank()) base = "http://localhost:8080";
        final String url = base.replaceAll("/+$", "") + context.getContextPath() + "/account/activate";
        ActivationEmailSender_24133057 sender = new ActivationEmailSender_24133057(ServiceFactory_24133057.emailService());
        executor = Executors.newSingleThreadScheduledExecutor(r -> { Thread t = new Thread(r, "video-activation-email"); t.setDaemon(true); return t; });
        executor.scheduleWithFixedDelay(() -> {
            try { for (int i = 0; i < 5 && sender.sendNext(url); i++) { /* bounded batch */ } }
            catch (Exception e) { context.log("Activation email delivery will retry", e); }
        }, 10, 30, TimeUnit.SECONDS);
    }
    @Override public void contextDestroyed(ServletContextEvent event) { if (executor != null) executor.shutdownNow(); }
}
