package huuthang.dao;

import huuthang.config.DatabaseConnection_24133057;
import huuthang.model.ActivationCode_24133057;
import java.sql.*;
import java.util.*;

public class ActivationDao_24133057 {
    private static final String CODE_SELECT="SELECT a.Code,a.Status,a.OrderId,p.VideoId,oi.VideoTitle FROM dbo.VideoActivationCodes a JOIN dbo.Products p ON p.ProductId=a.ProductId JOIN dbo.OrderItems oi ON oi.OrderId=a.OrderId AND oi.ProductId=a.ProductId ";
    public boolean requiresActivation(String videoId)throws SQLException {
        return exists("SELECT 1 FROM dbo.Products WHERE VideoId=?",videoId);
    }
    public boolean unlocked(String username,String videoId)throws SQLException {
        return username!=null && exists("SELECT 1 FROM dbo.VideoActivationCodes a JOIN dbo.Products p ON p.ProductId=a.ProductId JOIN dbo.Orders o ON o.OrderId=a.OrderId WHERE a.ActivatedBy=? AND p.VideoId=? AND a.Status='ACTIVATED' AND o.Status='DELIVERED' AND o.PaymentStatus='PAID'",username,videoId);
    }
    private boolean exists(String sql,String...values)throws SQLException {
        try(Connection c=DatabaseConnection_24133057.getConnection();PreparedStatement s=c.prepareStatement(sql)) {
            for(int i=0;i<values.length;i++)s.setNString(i+1,values[i]);try(ResultSet rs=s.executeQuery()){return rs.next();}
        }
    }
    public List<ActivationCode_24133057> orderCodes(String username,long orderId)throws SQLException {
        return codes(CODE_SELECT+"JOIN dbo.Orders o ON o.OrderId=a.OrderId WHERE o.Username=? AND o.OrderId=? ORDER BY a.ProductId,a.SerialNumber",username,orderId);
    }
    public List<ActivationCode_24133057> library(String username)throws SQLException {
        return codes(CODE_SELECT+"JOIN dbo.Orders o ON o.OrderId=a.OrderId WHERE a.ActivatedBy=? AND a.Status='ACTIVATED' AND o.Status='DELIVERED' AND o.PaymentStatus='PAID' ORDER BY a.ActivatedAt DESC",username);
    }
    private List<ActivationCode_24133057> codes(String sql,Object...values)throws SQLException {
        try(Connection c=DatabaseConnection_24133057.getConnection();PreparedStatement s=c.prepareStatement(sql)) {
            for(int i=0;i<values.length;i++)s.setObject(i+1,values[i]);List<ActivationCode_24133057> result=new ArrayList<>();
            try(ResultSet rs=s.executeQuery()){while(rs.next())result.add(new ActivationCode_24133057(rs.getString("Code"),rs.getNString("VideoTitle"),rs.getString("VideoId"),rs.getString("Status"),rs.getLong("OrderId")));}return result;
        }
    }
    public static String normalize(String code) {
        if (code != null && code.length() > 64) throw new IllegalArgumentException("Mã kích hoạt quá dài. Vui lòng sao chép đúng mã được gửi.");
        String normalized=code==null?"":code.strip().replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
        if(!normalized.matches("[0-9A-F]{32}"))throw new IllegalArgumentException("Mã kích hoạt phải có 32 ký tự chữ/số. Vui lòng sao chép đúng mã được gửi.");
        return normalized;
    }
    public String redeem(String username,String rawCode)throws SQLException {
        String code=normalize(rawCode);
        try(Connection c=DatabaseConnection_24133057.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement s=c.prepareStatement("SELECT Active FROM dbo.Users WITH(UPDLOCK,HOLDLOCK) WHERE Username=?")) {
                    s.setNString(1,username);try(ResultSet rs=s.executeQuery()){if(!rs.next()||!rs.getBoolean(1))throw new IllegalArgumentException("Tài khoản không còn hoạt động.");}
                }
                String videoId,state,activatedBy;
                try(PreparedStatement s=c.prepareStatement("SELECT p.VideoId,a.Status,a.ActivatedBy,o.Status AS OrderState,o.PaymentStatus FROM dbo.VideoActivationCodes a WITH(UPDLOCK,HOLDLOCK) JOIN dbo.Products p ON p.ProductId=a.ProductId JOIN dbo.Orders o WITH(HOLDLOCK) ON o.OrderId=a.OrderId WHERE a.Code=?")) {
                    s.setString(1,code);try(ResultSet rs=s.executeQuery()) {
                        if(!rs.next()||!"DELIVERED".equals(rs.getString("OrderState"))||!"PAID".equals(rs.getString("PaymentStatus")))throw new IllegalArgumentException("Mã không hợp lệ hoặc đơn COD chưa hoàn tất/đã hoàn.");
                        videoId=rs.getString("VideoId");state=rs.getString("Status");activatedBy=rs.getString("ActivatedBy");
                    }
                }
                if("ACTIVATED".equals(state)&&username.equals(activatedBy)){c.commit();return videoId;}
                if(!"AVAILABLE".equals(state))throw new IllegalArgumentException("Mã đã được sử dụng hoặc đã bị thu hồi.");
                try(PreparedStatement s=c.prepareStatement("SELECT 1 FROM dbo.VideoActivationCodes a JOIN dbo.Products p ON p.ProductId=a.ProductId JOIN dbo.Orders o ON o.OrderId=a.OrderId WHERE a.ActivatedBy=? AND p.VideoId=? AND a.Status='ACTIVATED' AND o.Status='DELIVERED' AND o.PaymentStatus='PAID'")) {
                    s.setNString(1,username);s.setNString(2,videoId);try(ResultSet rs=s.executeQuery()){if(rs.next())throw new IllegalArgumentException("Tài khoản đã kích hoạt video này. Mã mới chưa bị sử dụng.");}
                }
                try(PreparedStatement s=c.prepareStatement("UPDATE dbo.VideoActivationCodes SET Status='ACTIVATED',ActivatedBy=?,ActivatedAt=SYSUTCDATETIME() WHERE Code=? AND Status='AVAILABLE'")) {
                    s.setNString(1,username);s.setString(2,code);if(s.executeUpdate()!=1)throw new IllegalArgumentException("Mã không còn khả dụng.");
                }
                c.commit();return videoId;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    public String emailState(String username,long orderId)throws SQLException {
        try(Connection c=DatabaseConnection_24133057.getConnection();PreparedStatement s=c.prepareStatement("SELECT e.Status FROM dbo.ActivationEmailOutbox e JOIN dbo.Orders o ON o.OrderId=e.OrderId WHERE o.Username=? AND o.OrderId=?")) {
            s.setNString(1,username);s.setLong(2,orderId);try(ResultSet rs=s.executeQuery()){return rs.next()?rs.getString(1):"UNAVAILABLE";}
        }
    }
}
