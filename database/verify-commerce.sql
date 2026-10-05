/* Smoke test for the SQL constraints and status trigger. All fixture changes are rolled back. */
USE VideoManagement_24133057;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;
DECLARE @Username NVARCHAR(50) = N'commerce_sql_' + REPLACE(CONVERT(NVARCHAR(36), NEWID()),N'-',N'');
DECLARE @ProductId INT = (SELECT TOP 1 ProductId FROM dbo.Products WHERE Stock >= 3 ORDER BY ProductId);
IF @ProductId IS NULL THROW 51000, N'Need a seeded product with at least 3 stock.', 1;
INSERT dbo.Users(Username,Fullname,Active) VALUES(@Username,N'Nguyễn Văn An',1);
DECLARE @OriginalStock INT = (SELECT Stock FROM dbo.Products WHERE ProductId=@ProductId);
DECLARE @Price DECIMAL(18,0) = (SELECT Price FROM dbo.Products WHERE ProductId=@ProductId);
DECLARE @Title NVARCHAR(200) = (SELECT v.Title FROM dbo.Products p JOIN dbo.Videos v ON p.VideoId=v.VideoId WHERE p.ProductId=@ProductId);

UPDATE dbo.Products SET Stock=Stock-2 WHERE ProductId=@ProductId;
INSERT dbo.Orders(Username,CheckoutToken,Recipient,Phone,Address,Total)
VALUES(@Username,NEWID(),N'Nguyễn Văn An',N'0912345678',N'12 Nguyễn Văn Bảo, TP Hồ Chí Minh',2*@Price);
DECLARE @OrderId BIGINT = SCOPE_IDENTITY();
INSERT dbo.OrderItems(OrderId,ProductId,VideoTitle,UnitPrice,Quantity) VALUES(@OrderId,@ProductId,@Title,@Price,2);
IF NOT EXISTS(SELECT 1 FROM dbo.Orders WHERE OrderId=@OrderId AND Status='NEW' AND PaymentStatus='PENDING')
    THROW 51001, N'Initial COD state failed.', 1;
UPDATE dbo.Orders SET Status='CONFIRMED' WHERE OrderId=@OrderId;
UPDATE dbo.Orders SET Status='PREPARING' WHERE OrderId=@OrderId;
UPDATE dbo.Orders SET Status='SHIPPING' WHERE OrderId=@OrderId;
UPDATE dbo.Orders SET Status='OUT_FOR_DELIVERY' WHERE OrderId=@OrderId;
IF EXISTS(SELECT 1 FROM dbo.VideoActivationCodes WHERE OrderId=@OrderId)
    THROW 51008, N'Activation codes must not be issued before COD payment.', 1;
UPDATE dbo.Orders SET Status='DELIVERED' WHERE OrderId=@OrderId;
IF NOT EXISTS(SELECT 1 FROM dbo.Orders WHERE OrderId=@OrderId AND PaymentStatus='PAID')
    THROW 51002, N'Delivered COD payment failed.', 1;
IF (SELECT COUNT(*) FROM dbo.VideoActivationCodes WHERE OrderId=@OrderId AND Status='AVAILABLE') <> 2
    THROW 51009, N'Expected one activation code per purchased unit.', 1;
UPDATE dbo.Orders SET Status='RETURNED' WHERE OrderId=@OrderId;
UPDATE dbo.Orders SET Status='RETURNED' WHERE OrderId=@OrderId;
IF (SELECT Stock FROM dbo.Products WHERE ProductId=@ProductId) <> @OriginalStock
    THROW 51003, N'Return restock must happen once.', 1;
IF (SELECT COUNT(*) FROM dbo.OrderStatusHistory WHERE OrderId=@OrderId) <> 7
    THROW 51004, N'Status history is missing or duplicated.', 1;
IF NOT EXISTS(SELECT 1 FROM dbo.Orders WHERE OrderId=@OrderId AND PaymentStatus='REFUNDED')
    THROW 51005, N'Returned payment state failed.', 1;
IF EXISTS(SELECT 1 FROM dbo.VideoActivationCodes WHERE OrderId=@OrderId AND Status<>'REVOKED')
    THROW 51010, N'Returned activation codes must be revoked.', 1;

UPDATE dbo.Products SET Stock=Stock-1 WHERE ProductId=@ProductId;
INSERT dbo.Orders(Username,CheckoutToken,Recipient,Phone,Address,Total)
VALUES(@Username,NEWID(),N'Nguyễn Văn An',N'0912345678',N'12 Nguyễn Văn Bảo, TP Hồ Chí Minh',@Price);
DECLARE @CancelledId BIGINT = SCOPE_IDENTITY();
INSERT dbo.OrderItems(OrderId,ProductId,VideoTitle,UnitPrice,Quantity) VALUES(@CancelledId,@ProductId,@Title,@Price,1);
UPDATE dbo.Orders SET Status='CANCELLED' WHERE OrderId=@CancelledId;
UPDATE dbo.Orders SET Status='CANCELLED' WHERE OrderId=@CancelledId;
IF (SELECT Stock FROM dbo.Products WHERE ProductId=@ProductId) <> @OriginalStock
    THROW 51006, N'Cancel restock must happen once.', 1;
IF NOT EXISTS(SELECT 1 FROM dbo.Orders WHERE OrderId=@CancelledId AND PaymentStatus='VOID')
    THROW 51007, N'Cancelled payment state failed.', 1;
ROLLBACK TRANSACTION;
PRINT N'PASS: COD states, activation codes, return/cancel restock and status history.';
GO
