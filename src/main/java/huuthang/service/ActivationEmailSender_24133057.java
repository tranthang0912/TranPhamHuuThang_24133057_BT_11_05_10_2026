package huuthang.service;

import huuthang.dao.ActivationDao_24133057;
import huuthang.dao.ActivationEmailDao_24133057;
import java.sql.SQLException;

public class ActivationEmailSender_24133057 {
    private final EmailService_24133057 mail;
    private final ActivationEmailDao_24133057 queue;
    private final ActivationDao_24133057 codes;
    public ActivationEmailSender_24133057(EmailService_24133057 mail) {
        this(mail, new ActivationEmailDao_24133057(), new ActivationDao_24133057());
    }
    public ActivationEmailSender_24133057(EmailService_24133057 mail, ActivationEmailDao_24133057 queue, ActivationDao_24133057 codes) {
        this.mail = mail; this.queue = queue; this.codes = codes;
    }
    public boolean sendNext(String activationUrl) throws SQLException {
        if (!mail.isConfigured()) return false;
        var next = queue.claim(); if (next.isEmpty()) return false;
        var d = next.get(); boolean sent = false;
        try {
            var available = codes.orderCodes(d.username(), d.orderId()).stream().filter(a -> !"REVOKED".equals(a.getStatus())).toList();
            if (!available.isEmpty()) sent = mail.sendVideoActivationCodes(d.recipient(), d.fullname(), d.orderId(), available, activationUrl);
        } finally { queue.finish(d.outboxId(), sent); }
        return true;
    }
}
