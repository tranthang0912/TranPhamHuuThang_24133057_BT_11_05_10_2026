package huuthang.model;

public class ActivationCode_24133057 {
    private final String code,title,videoId,status;
    private final long orderId;
    public ActivationCode_24133057(String code,String title,String videoId,String status,long orderId) {
        this.code=code;this.title=title;this.videoId=videoId;this.status=status;this.orderId=orderId;
    }
    public String getCode(){return code;}
    public String getTitle(){return title;}
    public String getVideoId(){return videoId;}
    public String getStatus(){return status;}
    public long getOrderId(){return orderId;}
    public String getStatusLabel(){return switch(status){case "ACTIVATED"->"Đã kích hoạt";case "REVOKED"->"Đã thu hồi do hoàn đơn";default->"Chưa sử dụng";};}
}
