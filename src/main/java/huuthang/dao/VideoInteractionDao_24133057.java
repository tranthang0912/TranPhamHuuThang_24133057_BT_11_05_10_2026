package huuthang.dao;

import huuthang.config.DatabaseConnection_24133057;
import huuthang.dao.impl.VideoDaoImpl_24133057;
import huuthang.model.VideoView_24133057;
import java.sql.*;
import java.util.*;

public class VideoInteractionDao_24133057 {
    public boolean liked(String username,String videoId)throws SQLException {
        try(Connection c=DatabaseConnection_24133057.getConnection();PreparedStatement s=c.prepareStatement("SELECT 1 FROM dbo.Favorites WHERE Username=? AND VideoId=?")) {
            s.setNString(1,username);s.setNString(2,videoId);try(ResultSet rs=s.executeQuery()){return rs.next();}
        }
    }
    // A desired state makes retries and simultaneous clicks idempotent, unlike an unqualified toggle.
    public void favorite(String username,String videoId,boolean desired)throws SQLException {
        mutate(username,videoId,c->{
            if(desired) {
                try(PreparedStatement s=c.prepareStatement("INSERT dbo.Favorites(Username,VideoId) SELECT ?,? WHERE NOT EXISTS(SELECT 1 FROM dbo.Favorites WHERE Username=? AND VideoId=?)")) {
                    s.setNString(1,username);s.setNString(2,videoId);s.setNString(3,username);s.setNString(4,videoId);s.executeUpdate();
                }
            } else try(PreparedStatement s=c.prepareStatement("DELETE dbo.Favorites WHERE Username=? AND VideoId=?")) {
                s.setNString(1,username);s.setNString(2,videoId);s.executeUpdate();
            }
        });
    }
    public void share(String username,String videoId)throws SQLException {
        mutate(username,videoId,c->{
            try(PreparedStatement s=c.prepareStatement("INSERT dbo.Shares(Username,VideoId,SharedDate) SELECT ?,?,CONVERT(date,DATEADD(hour,7,SYSUTCDATETIME())) WHERE NOT EXISTS(SELECT 1 FROM dbo.Shares WHERE Username=? AND VideoId=? AND SharedDate=CONVERT(date,DATEADD(hour,7,SYSUTCDATETIME())))")) {
                s.setNString(1,username);s.setNString(2,videoId);s.setNString(3,username);s.setNString(4,videoId);s.executeUpdate();
            }
        });
    }
    public List<VideoView_24133057> favorites(String username)throws SQLException {
        List<String> ids=new ArrayList<>();
        try(Connection c=DatabaseConnection_24133057.getConnection();PreparedStatement s=c.prepareStatement("SELECT f.VideoId FROM dbo.Favorites f JOIN dbo.Videos v ON f.VideoId=v.VideoId WHERE f.Username=? AND v.Active=1 ORDER BY f.LikedDate DESC,f.FavoriteId DESC")) {
            s.setNString(1,username);try(ResultSet rs=s.executeQuery()){while(rs.next())ids.add(rs.getString(1));}
        }
        List<VideoView_24133057> videos=new ArrayList<>();VideoDaoImpl_24133057 dao=new VideoDaoImpl_24133057();
        for(String id:ids)dao.findById(id).ifPresent(videos::add);return videos;
    }
    private void mutate(String username,String videoId,Work work)throws SQLException {
        try(Connection c=DatabaseConnection_24133057.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement s=c.prepareStatement("SELECT Active FROM dbo.Users WITH(UPDLOCK,HOLDLOCK) WHERE Username=?")) {
                    s.setNString(1,username);try(ResultSet rs=s.executeQuery()){if(!rs.next()||!rs.getBoolean(1))throw new IllegalArgumentException("Tài khoản đã bị khóa hoặc không tồn tại.");}
                }
                try(PreparedStatement s=c.prepareStatement("SELECT 1 FROM dbo.Videos WITH(HOLDLOCK) WHERE VideoId=? AND Active=1")) {
                    s.setNString(1,videoId);try(ResultSet rs=s.executeQuery()){if(!rs.next())throw new IllegalArgumentException("Video không tồn tại hoặc đã ngừng hoạt động.");}
                }
                work.run(c);c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    @FunctionalInterface private interface Work{void run(Connection c)throws SQLException;}
}
