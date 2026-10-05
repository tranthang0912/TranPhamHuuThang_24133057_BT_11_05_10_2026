USE VideoManagement_24133057;
GO
-- 1. Find the ID of a NEW order belonging to your signed-in account.
SELECT OrderId, Username, Status, PaymentStatus, Total, CreatedAt
FROM dbo.Orders ORDER BY OrderId DESC;

-- 2. Replace 0 with that ID. Execute ONE update at a time, then click
--    "Làm mới trạng thái" on /orders or /orders/detail?id=... .
DECLARE @OrderId BIGINT = 0;
IF @OrderId <= 0 THROW 50002, N'Điền mã đơn hàng cần kiểm tra trước khi chạy.', 1;

UPDATE dbo.Orders SET Status = 'CONFIRMED' WHERE OrderId = @OrderId;
-- Đã xác nhận

UPDATE dbo.Orders SET Status = 'PREPARING' WHERE OrderId = @OrderId;
-- Chuẩn bị hàng

UPDATE dbo.Orders SET Status = 'SHIPPING' WHERE OrderId = @OrderId;
-- Vận chuyển

UPDATE dbo.Orders SET Status = 'OUT_FOR_DELIVERY' WHERE OrderId = @OrderId;
-- Đang giao hàng

UPDATE dbo.Orders SET Status = 'DELIVERED' WHERE OrderId = @OrderId;
-- Đã giao: COD chuyển thành PAID, cấp mã theo số bộ và xếp email vào hàng gửi.
-- Mở chi tiết đơn để lấy mã; đăng nhập rồi vào /account/activate để kích hoạt video.
SELECT a.Code,a.Status,p.VideoId,oi.VideoTitle
FROM dbo.VideoActivationCodes a JOIN dbo.Products p ON p.ProductId=a.ProductId
JOIN dbo.OrderItems oi ON oi.OrderId=a.OrderId AND oi.ProductId=a.ProductId
WHERE a.OrderId=@OrderId;

UPDATE dbo.Orders SET Status = 'RETURNED' WHERE OrderId = @OrderId;
-- Đơn hàng hoàn: hoàn số lượng, ghi REFUNDED, thu hồi mã và quyền xem.

SELECT * FROM dbo.OrderStatusHistory WHERE OrderId = @OrderId ORDER BY HistoryId;

-- To test cancellation, use ANOTHER order in NEW or CONFIRMED:
-- UPDATE dbo.Orders SET Status = 'CANCELLED' WHERE OrderId = <ID_OF_NEW_ORDER>;
-- The trigger restores stock once and sets payment to VOID.
-- CANCELLED and RETURNED cannot be reopened; create another order to repeat a demo.
