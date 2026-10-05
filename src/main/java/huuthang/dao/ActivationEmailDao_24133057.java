package huuthang.dao;

import huuthang.config.DatabaseConnection_24133057;
import java.sql.*;
import java.util.Optional;

public class ActivationEmailDao_24133057 {
    public record Delivery(long outboxId, long orderId, String username, String fullname, String recipient) { }
    // Claims are short transactions; SMTP runs outside the transaction. A lease recovers interrupted sends.
    public Optional<Delivery> claim() throws SQLException {
        try (Connection c = DatabaseConnection_24133057.getConnection()) {
            c.setAutoCommit(false);
            try {
                Delivery delivery = null;
                try (PreparedStatement s = c.prepareStatement("SELECT TOP(1) e.OutboxId,e.OrderId,o.Username,u.Fullname,e.Recipient FROM dbo.ActivationEmailOutbox e WITH(UPDLOCK,READPAST,ROWLOCK) JOIN dbo.Orders o ON o.OrderId=e.OrderId JOIN dbo.Users u ON u.Username=o.Username WHERE e.Status IN ('PENDING','FAILED','SENDING') AND e.Attempts<5 AND e.NextAttemptAt<=SYSUTCDATETIME() AND o.Status='DELIVERED' AND o.PaymentStatus='PAID' ORDER BY e.OutboxId"); ResultSet rs = s.executeQuery()) {
                    if (rs.next()) delivery = new Delivery(rs.getLong(1), rs.getLong(2), rs.getNString(3), rs.getNString(4), rs.getNString(5));
                }
                if (delivery != null) try (PreparedStatement s = c.prepareStatement("UPDATE dbo.ActivationEmailOutbox SET Status='SENDING',Attempts=Attempts+1,NextAttemptAt=DATEADD(minute,5,SYSUTCDATETIME()) WHERE OutboxId=?")) {
                    s.setLong(1, delivery.outboxId()); s.executeUpdate();
                }
                c.commit(); return Optional.ofNullable(delivery);
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    public void finish(long id, boolean sent) throws SQLException {
        String sql = sent
                ? "UPDATE dbo.ActivationEmailOutbox SET Status='SENT',SentAt=SYSUTCDATETIME() WHERE OutboxId=? AND Status='SENDING'"
                : "UPDATE dbo.ActivationEmailOutbox SET Status='FAILED',NextAttemptAt=DATEADD(minute,POWER(2,Attempts),SYSUTCDATETIME()) WHERE OutboxId=? AND Status='SENDING'";
        try (Connection c = DatabaseConnection_24133057.getConnection(); PreparedStatement s = c.prepareStatement(sql)) { s.setLong(1, id); s.executeUpdate(); }
    }
}
