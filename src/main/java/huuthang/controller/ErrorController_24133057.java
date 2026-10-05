package huuthang.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ErrorController_24133057 extends BaseController_24133057 {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object message = request.getAttribute("javax.servlet.error.message");
        Map<String, Object> model = new HashMap<>();
        int code = status instanceof Integer ? (Integer) status : 500;
        response.setStatus(code);
        model.put("statusCode", code);
        model.put("errorTitle", switch (code) {
            case 404 -> "Không tìm thấy trang";
            case 403 -> "Bạn chưa có quyền truy cập";
            case 405 -> "Thao tác chưa được hỗ trợ";
            case 429 -> "Vui lòng thử lại sau";
            case 503 -> "Dịch vụ đang tạm nghỉ";
            default -> "Trang này đang tạm nghỉ";
        });
        model.put("errorMessage", message == null || message.toString().isBlank()
                ? "Đã có lỗi xảy ra. Vui lòng thử lại." : message);
        render(request, response, "error", model);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
