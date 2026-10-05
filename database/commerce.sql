/* Run after schema-and-data.sql. Safe to rerun: existing prices, stock and orders are preserved. */
USE VideoManagement_24133057;
GO
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.Products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Products (
        ProductId INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Products PRIMARY KEY,
        VideoId NVARCHAR(50) NOT NULL CONSTRAINT UQ_Products_Video UNIQUE,
        Price DECIMAL(18,0) NOT NULL,
        Stock INT NOT NULL CONSTRAINT DF_Products_Stock DEFAULT (0),
        Active BIT NOT NULL CONSTRAINT DF_Products_Active DEFAULT (1),
        CONSTRAINT FK_Products_Videos FOREIGN KEY (VideoId) REFERENCES dbo.Videos(VideoId),
        CONSTRAINT CK_Products_Price CHECK (Price BETWEEN 1 AND 100000000),
        CONSTRAINT CK_Products_Stock CHECK (Stock BETWEEN 0 AND 1000000)
    );
END;

IF OBJECT_ID(N'dbo.CartItems', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.CartItems (
        Username NVARCHAR(50) NOT NULL,
        ProductId INT NOT NULL,
        Quantity INT NOT NULL,
        UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_CartItems_Updated DEFAULT SYSUTCDATETIME(),
        CONSTRAINT PK_CartItems PRIMARY KEY (Username, ProductId),
        CONSTRAINT FK_CartItems_Users FOREIGN KEY (Username) REFERENCES dbo.Users(Username) ON DELETE CASCADE,
        CONSTRAINT FK_CartItems_Products FOREIGN KEY (ProductId) REFERENCES dbo.Products(ProductId),
        CONSTRAINT CK_CartItems_Quantity CHECK (Quantity BETWEEN 1 AND 99)
    );
END;

IF OBJECT_ID(N'dbo.Orders', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Orders (
        OrderId BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Orders PRIMARY KEY,
        Username NVARCHAR(50) NOT NULL,
        CheckoutToken UNIQUEIDENTIFIER NOT NULL CONSTRAINT UQ_Orders_CheckoutToken UNIQUE,
        Recipient NVARCHAR(100) NOT NULL,
        Phone NVARCHAR(15) NOT NULL,
        Address NVARCHAR(500) NOT NULL,
        Note NVARCHAR(500) NOT NULL CONSTRAINT DF_Orders_Note DEFAULT N'',
        Total DECIMAL(18,0) NOT NULL,
        Status VARCHAR(20) NOT NULL CONSTRAINT DF_Orders_Status DEFAULT 'NEW',
        PaymentMethod VARCHAR(10) NOT NULL CONSTRAINT DF_Orders_Method DEFAULT 'COD',
        PaymentStatus VARCHAR(10) NOT NULL CONSTRAINT DF_Orders_Payment DEFAULT 'PENDING',
        CreatedAt DATETIME2 NOT NULL CONSTRAINT DF_Orders_Created DEFAULT SYSUTCDATETIME(),
        UpdatedAt DATETIME2 NOT NULL CONSTRAINT DF_Orders_Updated DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_Orders_Users FOREIGN KEY (Username) REFERENCES dbo.Users(Username),
        CONSTRAINT CK_Orders_Total CHECK (Total > 0),
        CONSTRAINT CK_Orders_Recipient CHECK (LEN(LTRIM(RTRIM(Recipient))) BETWEEN 2 AND 100),
        CONSTRAINT CK_Orders_Address CHECK (LEN(LTRIM(RTRIM(Address))) BETWEEN 10 AND 500),
        CONSTRAINT CK_Orders_Phone CHECK (LEN(Phone) BETWEEN 10 AND 12),
        CONSTRAINT CK_Orders_Status CHECK (Status IN
            ('NEW','CONFIRMED','PREPARING','SHIPPING','OUT_FOR_DELIVERY','DELIVERED','CANCELLED','RETURNED')),
        CONSTRAINT CK_Orders_Method CHECK (PaymentMethod = 'COD'),
        CONSTRAINT CK_Orders_Payment CHECK (PaymentStatus IN ('PENDING','PAID','VOID','REFUNDED'))
    );
    CREATE INDEX IX_Orders_UserStatus ON dbo.Orders(Username, Status, CreatedAt DESC, OrderId DESC);
END;

IF OBJECT_ID(N'dbo.OrderItems', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.OrderItems (
        OrderId BIGINT NOT NULL,
        ProductId INT NOT NULL,
        VideoTitle NVARCHAR(200) NOT NULL,
        UnitPrice DECIMAL(18,0) NOT NULL,
        Quantity INT NOT NULL,
        CONSTRAINT PK_OrderItems PRIMARY KEY (OrderId, ProductId),
        CONSTRAINT FK_OrderItems_Orders FOREIGN KEY (OrderId) REFERENCES dbo.Orders(OrderId),
        CONSTRAINT FK_OrderItems_Products FOREIGN KEY (ProductId) REFERENCES dbo.Products(ProductId),
        CONSTRAINT CK_OrderItems_Quantity CHECK (Quantity BETWEEN 1 AND 99),
        CONSTRAINT CK_OrderItems_Price CHECK (UnitPrice BETWEEN 1 AND 100000000)
    );
END;

IF OBJECT_ID(N'dbo.OrderStatusHistory', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.OrderStatusHistory (
        HistoryId BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_OrderStatusHistory PRIMARY KEY,
        OrderId BIGINT NOT NULL,
        Status VARCHAR(20) NOT NULL,
        ChangedAt DATETIME2 NOT NULL CONSTRAINT DF_OrderHistory_Changed DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_OrderHistory_Orders FOREIGN KEY (OrderId) REFERENCES dbo.Orders(OrderId),
        CONSTRAINT CK_OrderHistory_Status CHECK (Status IN
            ('NEW','CONFIRMED','PREPARING','SHIPPING','OUT_FOR_DELIVERY','DELIVERED','CANCELLED','RETURNED'))
    );
    CREATE INDEX IX_OrderHistory_Order ON dbo.OrderStatusHistory(OrderId, HistoryId);
END;

IF OBJECT_ID(N'dbo.VideoActivationCodes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.VideoActivationCodes (
        ActivationId BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_VideoActivationCodes PRIMARY KEY,
        OrderId BIGINT NOT NULL,
        ProductId INT NOT NULL,
        SerialNumber INT NOT NULL,
        Code CHAR(32) NOT NULL CONSTRAINT UQ_VideoActivationCodes_Code UNIQUE,
        Status VARCHAR(12) NOT NULL CONSTRAINT DF_VideoActivationCodes_Status DEFAULT 'AVAILABLE',
        ActivatedBy NVARCHAR(50) NULL,
        ActivatedAt DATETIME2 NULL,
        CONSTRAINT UQ_VideoActivationCodes_Item UNIQUE(OrderId,ProductId,SerialNumber),
        CONSTRAINT FK_VideoActivationCodes_Item FOREIGN KEY(OrderId,ProductId) REFERENCES dbo.OrderItems(OrderId,ProductId),
        CONSTRAINT FK_VideoActivationCodes_User FOREIGN KEY(ActivatedBy) REFERENCES dbo.Users(Username),
        CONSTRAINT CK_VideoActivationCodes_Serial CHECK(SerialNumber BETWEEN 1 AND 99),
        CONSTRAINT CK_VideoActivationCodes_Status CHECK(Status IN ('AVAILABLE','ACTIVATED','REVOKED'))
    );
    CREATE INDEX IX_VideoActivationCodes_User ON dbo.VideoActivationCodes(ActivatedBy,Status,ProductId);
END;

IF OBJECT_ID(N'dbo.ActivationEmailOutbox', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ActivationEmailOutbox (
        OutboxId BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_ActivationEmailOutbox PRIMARY KEY,
        OrderId BIGINT NOT NULL CONSTRAINT UQ_ActivationEmailOutbox_Order UNIQUE,
        Recipient NVARCHAR(150) NOT NULL,
        Status VARCHAR(12) NOT NULL CONSTRAINT DF_ActivationEmailOutbox_Status DEFAULT 'PENDING',
        Attempts INT NOT NULL CONSTRAINT DF_ActivationEmailOutbox_Attempts DEFAULT 0,
        NextAttemptAt DATETIME2 NOT NULL CONSTRAINT DF_ActivationEmailOutbox_Next DEFAULT SYSUTCDATETIME(),
        SentAt DATETIME2 NULL,
        CONSTRAINT FK_ActivationEmailOutbox_Order FOREIGN KEY(OrderId) REFERENCES dbo.Orders(OrderId),
        CONSTRAINT CK_ActivationEmailOutbox_Status CHECK(Status IN ('PENDING','SENDING','SENT','FAILED','CANCELLED')),
        CONSTRAINT CK_ActivationEmailOutbox_Attempts CHECK(Attempts BETWEEN 0 AND 10)
    );
END;

-- Learning videos already present in this project become products; entertainment videos stay in the library.
INSERT dbo.Products (VideoId, Price, Stock, Active)
SELECT v.VideoId, CASE c.Categorycode WHEN N'CONG-NGHE' THEN 199000 ELSE 149000 END, 50, 1
FROM dbo.Videos v JOIN dbo.Category c ON c.CategoryId = v.CategoryId
WHERE c.Categorycode IN (N'CONG-NGHE', N'GIAO-DUC')
    AND NOT EXISTS (SELECT 1 FROM dbo.Products p WHERE p.VideoId = v.VideoId);
GO

CREATE OR ALTER TRIGGER dbo.TR_Orders_Status
ON dbo.Orders AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF NOT (UPDATE(Status)) RETURN;

    -- Sequential fulfillment; early cancellation; returns only after delivery.
    IF EXISTS (
        SELECT 1 FROM inserted i LEFT JOIN deleted d ON d.OrderId = i.OrderId
        WHERE (d.OrderId IS NULL AND i.Status <> 'NEW') OR
            (d.OrderId IS NOT NULL AND i.Status <> d.Status AND NOT (
                (d.Status = 'NEW' AND i.Status IN ('CONFIRMED','CANCELLED')) OR
                (d.Status = 'CONFIRMED' AND i.Status IN ('PREPARING','CANCELLED')) OR
                (d.Status = 'PREPARING' AND i.Status = 'SHIPPING') OR
                (d.Status = 'SHIPPING' AND i.Status = 'OUT_FOR_DELIVERY') OR
                (d.Status = 'OUT_FOR_DELIVERY' AND i.Status = 'DELIVERED') OR
                (d.Status = 'DELIVERED' AND i.Status = 'RETURNED')
            )))
    BEGIN
        ;THROW 50001, N'Trạng thái không hợp lệ: chỉ chuyển theo quy trình; hủy khi mới/xác nhận, hoàn khi đã giao.', 1;
    END;

    -- Terminal states cannot be reopened, so each item's stock is restored exactly once.
    ;WITH Restock AS (
        SELECT oi.ProductId, SUM(oi.Quantity) AS Quantity
        FROM inserted i JOIN deleted d ON d.OrderId = i.OrderId
            JOIN dbo.OrderItems oi ON oi.OrderId = i.OrderId
        WHERE i.Status IN ('CANCELLED','RETURNED') AND i.Status <> d.Status
        GROUP BY oi.ProductId
    )
    UPDATE p SET Stock = p.Stock + r.Quantity
    FROM dbo.Products p JOIN Restock r ON r.ProductId = p.ProductId;

    UPDATE o SET UpdatedAt = SYSUTCDATETIME(), PaymentStatus = CASE i.Status
        WHEN 'DELIVERED' THEN 'PAID' WHEN 'CANCELLED' THEN 'VOID'
        WHEN 'RETURNED' THEN 'REFUNDED' ELSE 'PENDING' END
    FROM dbo.Orders o JOIN inserted i ON i.OrderId = o.OrderId
        LEFT JOIN deleted d ON d.OrderId = i.OrderId
    WHERE d.OrderId IS NULL OR d.Status <> i.Status;

    INSERT dbo.OrderStatusHistory(OrderId, Status)
    SELECT i.OrderId, i.Status FROM inserted i LEFT JOIN deleted d ON d.OrderId = i.OrderId
    WHERE d.OrderId IS NULL OR d.Status <> i.Status;

    -- Each purchased unit gets one single-use activation code only after COD was collected.
    ;WITH Numbers AS (SELECT 1 AS n UNION ALL SELECT n+1 FROM Numbers WHERE n<99)
    INSERT dbo.VideoActivationCodes(OrderId,ProductId,SerialNumber,Code)
    SELECT i.OrderId,oi.ProductId,n.n,UPPER(REPLACE(CONVERT(VARCHAR(36),NEWID()),'-',''))
    FROM inserted i JOIN deleted d ON d.OrderId=i.OrderId
        JOIN dbo.OrderItems oi ON oi.OrderId=i.OrderId CROSS JOIN Numbers n
    WHERE i.Status='DELIVERED' AND d.Status<>i.Status AND n.n<=oi.Quantity
        AND NOT EXISTS(SELECT 1 FROM dbo.VideoActivationCodes a WHERE a.OrderId=i.OrderId AND a.ProductId=oi.ProductId AND a.SerialNumber=n.n);

    INSERT dbo.ActivationEmailOutbox(OrderId,Recipient)
    SELECT i.OrderId,u.Email FROM inserted i JOIN deleted d ON d.OrderId=i.OrderId
        JOIN dbo.Users u ON u.Username=i.Username
    WHERE i.Status='DELIVERED' AND d.Status<>i.Status AND u.Email LIKE N'%_@_%._%'
        AND NOT EXISTS(SELECT 1 FROM dbo.ActivationEmailOutbox e WHERE e.OrderId=i.OrderId);

    UPDATE a SET Status='REVOKED' FROM dbo.VideoActivationCodes a JOIN inserted i ON i.OrderId=a.OrderId
    WHERE i.Status='RETURNED';
    UPDATE e SET Status='CANCELLED' FROM dbo.ActivationEmailOutbox e JOIN inserted i ON i.OrderId=e.OrderId
    WHERE i.Status='RETURNED' AND e.Status<>'SENT';
END;
GO
-- Supply codes for already completed COD orders when this migration is upgraded.
;WITH Numbers AS (SELECT 1 AS n UNION ALL SELECT n+1 FROM Numbers WHERE n<99)
INSERT dbo.VideoActivationCodes(OrderId,ProductId,SerialNumber,Code)
SELECT o.OrderId,oi.ProductId,n.n,UPPER(REPLACE(CONVERT(VARCHAR(36),NEWID()),'-',''))
FROM dbo.Orders o JOIN dbo.OrderItems oi ON oi.OrderId=o.OrderId CROSS JOIN Numbers n
WHERE o.Status='DELIVERED' AND o.PaymentStatus='PAID' AND n.n<=oi.Quantity
    AND NOT EXISTS(SELECT 1 FROM dbo.VideoActivationCodes a WHERE a.OrderId=o.OrderId AND a.ProductId=oi.ProductId AND a.SerialNumber=n.n);
INSERT dbo.ActivationEmailOutbox(OrderId,Recipient)
SELECT o.OrderId,u.Email FROM dbo.Orders o JOIN dbo.Users u ON u.Username=o.Username
WHERE o.Status='DELIVERED' AND o.PaymentStatus='PAID' AND u.Email LIKE N'%_@_%._%'
    AND NOT EXISTS(SELECT 1 FROM dbo.ActivationEmailOutbox e WHERE e.OrderId=o.OrderId);
GO
COMMIT TRANSACTION;
GO
