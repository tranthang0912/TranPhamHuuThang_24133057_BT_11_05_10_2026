/*
  SQL Server database for exam 04 - MSSV 24133057.
  Open this script in SQL Server Management Studio and run with your local credentials.
*/
USE master;
GO

IF DB_ID(N'VideoManagement_24133057') IS NULL
BEGIN
    CREATE DATABASE VideoManagement_24133057;
END;
GO

USE VideoManagement_24133057;
GO

IF OBJECT_ID(N'dbo.VideoIdSequence', N'SO') IS NULL
BEGIN
    CREATE SEQUENCE dbo.VideoIdSequence AS INT START WITH 1 INCREMENT BY 1;
END;
GO

IF OBJECT_ID(N'dbo.Users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Users (
        Username NVARCHAR(50) NOT NULL,
        Password NVARCHAR(50) NULL,
        Phone NVARCHAR(15) NULL,
        Fullname NVARCHAR(50) NULL,
        Email NVARCHAR(150) NULL,
        Admin BIT NULL CONSTRAINT DF_Users_Admin DEFAULT (0),
        Active BIT NULL CONSTRAINT DF_Users_Active DEFAULT (0),
        Images NVARCHAR(500) NULL,
        CONSTRAINT PK_Users PRIMARY KEY (Username)
    );
END;
GO

IF OBJECT_ID(N'dbo.Category', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Category (
        CategoryId INT IDENTITY(1,1) NOT NULL,
        Categoryname NVARCHAR(100) NULL,
        Categorycode NVARCHAR(100) NULL,
        Images NVARCHAR(500) NULL,
        Status BIT NULL CONSTRAINT DF_Category_Status DEFAULT (1),
        CONSTRAINT PK_Category PRIMARY KEY (CategoryId),
        CONSTRAINT UQ_Category_Categorycode UNIQUE (Categorycode)
    );
END;
GO

IF OBJECT_ID(N'dbo.Videos', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Videos (
        VideoId NVARCHAR(50) NOT NULL
            CONSTRAINT DF_Videos_VideoId DEFAULT
            (N'VID' + RIGHT(N'00000000' + CONVERT(NVARCHAR(8), NEXT VALUE FOR dbo.VideoIdSequence), 8)),
        Title NVARCHAR(200) NULL,
        Poster NVARCHAR(50) NULL,
        Views INT NULL CONSTRAINT DF_Videos_Views DEFAULT (0),
        Description NVARCHAR(500) NULL,
        Active BIT NULL CONSTRAINT DF_Videos_Active DEFAULT (1),
        CategoryId INT NULL,
        CONSTRAINT PK_Videos PRIMARY KEY (VideoId),
        CONSTRAINT FK_Videos_Category FOREIGN KEY (CategoryId)
            REFERENCES dbo.Category(CategoryId)
    );
END;
GO

IF OBJECT_ID(N'dbo.Shares', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Shares (
        ShareId INT IDENTITY(1,1) NOT NULL,
        Emails NVARCHAR(50) NULL,
        SharedDate DATE NULL CONSTRAINT DF_Shares_SharedDate DEFAULT (CONVERT(DATE, GETDATE())),
        Username NVARCHAR(50) NULL,
        VideoId NVARCHAR(50) NULL,
        CONSTRAINT PK_Shares PRIMARY KEY (ShareId),
        CONSTRAINT FK_Shares_Users FOREIGN KEY (Username)
            REFERENCES dbo.Users(Username) ON DELETE CASCADE,
        CONSTRAINT FK_Shares_Videos FOREIGN KEY (VideoId)
            REFERENCES dbo.Videos(VideoId) ON DELETE CASCADE
    );
END;
GO

IF OBJECT_ID(N'dbo.Favorites', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Favorites (
        FavoriteId INT IDENTITY(1,1) NOT NULL,
        LikedDate DATE NULL CONSTRAINT DF_Favorites_LikedDate DEFAULT (CONVERT(DATE, GETDATE())),
        VideoId NVARCHAR(50) NULL,
        Username NVARCHAR(50) NULL,
        CONSTRAINT PK_Favorites PRIMARY KEY (FavoriteId),
        CONSTRAINT FK_Favorites_Videos FOREIGN KEY (VideoId)
            REFERENCES dbo.Videos(VideoId) ON DELETE CASCADE,
        CONSTRAINT FK_Favorites_Users FOREIGN KEY (Username)
            REFERENCES dbo.Users(Username) ON DELETE CASCADE,
        CONSTRAINT UQ_Favorites_UserVideo UNIQUE (Username, VideoId)
    );
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = N'admin')
BEGIN
    INSERT dbo.Users (Username, Password, Phone, Fullname, Email, Admin, Active, Images)
    VALUES (N'admin', N'6G94qKPK8LYNjnTllCqm2G3BUM08AzOK7yW30tfjrMc=', N'0900000001',
            N'Quản trị viên', N'admin@huuthang.local', 1, 1, N'/assets/images/avatar-admin.svg');
END;

IF NOT EXISTS (SELECT 1 FROM dbo.Users WHERE Username = N'huuthang')
BEGIN
    INSERT dbo.Users (Username, Password, Phone, Fullname, Email, Admin, Active, Images)
    VALUES (N'huuthang', N'PnwZV2SIhigW8TtRLKzz5LqX3ZckPqC9airRZC2GunI=', N'0912345678',
            N'Trần Phạm Hữu Thắng', N'huuthang@example.com', 0, 1, N'/assets/images/avatar-user.svg');
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Category)
BEGIN
    INSERT dbo.Category (Categoryname, Categorycode, Images, Status) VALUES
        (N'Công nghệ', N'CONG-NGHE', N'/assets/images/category-tech.svg', 1),
        (N'Giáo dục', N'GIAO-DUC', N'/assets/images/category-education.svg', 1),
        (N'Giải trí', N'GIAI-TRI', N'/assets/images/category-entertainment.svg', 1);
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Videos)
BEGIN
    DECLARE @Tech INT = (SELECT CategoryId FROM dbo.Category WHERE Categorycode = N'CONG-NGHE');
    DECLARE @Edu INT = (SELECT CategoryId FROM dbo.Category WHERE Categorycode = N'GIAO-DUC');
    DECLARE @Fun INT = (SELECT CategoryId FROM dbo.Category WHERE Categorycode = N'GIAI-TRI');

    INSERT dbo.Videos (Title, Poster, Views, Description, Active, CategoryId) VALUES
        (N'Learn JavaScript - Full Course for Beginners', N'https://i.ytimg.com/vi/PkZNo7MFNFg/hqdefault.jpg', 1280, N'Khóa JavaScript nhập môn thực tế từ kênh freeCodeCamp.org.', 1, @Tech),
        (N'SQL Tutorial - Full Database Course for Beginners', N'https://i.ytimg.com/vi/HXV3zeQKqGY/hqdefault.jpg', 960, N'Khóa cơ sở dữ liệu và SQL dành cho người mới từ kênh freeCodeCamp.org.', 1, @Tech),
        (N'Learn Python - Full Course for Beginners [Tutorial]', N'https://i.ytimg.com/vi/rfscVS0vtbw/hqdefault.jpg', 840, N'Khóa Python cơ bản đầy đủ từ kênh freeCodeCamp.org.', 1, @Tech),
        (N'HTML Crash Course For Absolute Beginners', N'https://i.ytimg.com/vi/UB1O30fR-EE/hqdefault.jpg', 720, N'Hướng dẫn HTML cho người mới bắt đầu từ kênh Traversy Media.', 1, @Tech),
        (N'Marty Lobdell - Study Less Study Smart', N'https://i.ytimg.com/vi/IlU-zDU6aQ0/hqdefault.jpg', 650, N'Bài giảng phương pháp học tập hiệu quả của Marty Lobdell do Pierce College đăng tải.', 1, @Edu),
        (N'Inside the Mind of a Master Procrastinator | Tim Urban | TED', N'https://i.ytimg.com/vi/arj7oStGLkU/hqdefault.jpg', 590, N'Bài nói chuyện TED của Tim Urban về tâm lý trì hoãn.', 1, @Edu),
        (N'The first 20 hours -- how to learn anything | Josh Kaufman | TEDxCSU', N'https://i.ytimg.com/vi/5MgBikgcWnY/hqdefault.jpg', 510, N'Bài nói chuyện TEDx của Josh Kaufman về cách học một kỹ năng mới.', 1, @Edu),
        (N'Big Buck Bunny 60fps 4K - Official Blender Foundation Short Film', N'https://i.ytimg.com/vi/aqz-KE-bpKQ/hqdefault.jpg', 1420, N'Phim hoạt hình ngắn chính thức của Blender Foundation, đăng bởi kênh Blender.', 1, @Fun),
        (N'Me at the zoo', N'https://i.ytimg.com/vi/jNQXAC9IVRw/hqdefault.jpg', 1110, N'Video Me at the zoo của kênh jawed, một nội dung nổi tiếng trong lịch sử YouTube.', 1, @Fun),
        (N'Luis Fonsi - Despacito ft. Daddy Yankee', N'https://i.ytimg.com/vi/kJQP7kiw5Fk/hqdefault.jpg', 980, N'Video âm nhạc Despacito của Luis Fonsi và Daddy Yankee từ kênh LuisFonsiVEVO.', 1, @Fun);
END;
GO

/* Keep seeded Vietnamese text correct even when the script is rerun on an existing database. */
UPDATE dbo.Users SET Fullname = N'Quản trị viên' WHERE Username = N'admin';
UPDATE dbo.Users SET Fullname = N'Trần Phạm Hữu Thắng' WHERE Username = N'huuthang';
UPDATE dbo.Category SET Categoryname = N'Công nghệ' WHERE Categorycode = N'CONG-NGHE';
UPDATE dbo.Category SET Categoryname = N'Giáo dục' WHERE Categorycode = N'GIAO-DUC';
UPDATE dbo.Category SET Categoryname = N'Giải trí' WHERE Categorycode = N'GIAI-TRI';

UPDATE dbo.Videos SET
    Title = CASE VideoId
        WHEN N'VID00000001' THEN N'Learn JavaScript - Full Course for Beginners'
        WHEN N'VID00000002' THEN N'SQL Tutorial - Full Database Course for Beginners'
        WHEN N'VID00000003' THEN N'Learn Python - Full Course for Beginners [Tutorial]'
        WHEN N'VID00000004' THEN N'HTML Crash Course For Absolute Beginners'
        WHEN N'VID00000005' THEN N'Marty Lobdell - Study Less Study Smart'
        WHEN N'VID00000006' THEN N'Inside the Mind of a Master Procrastinator | Tim Urban | TED'
        WHEN N'VID00000007' THEN N'The first 20 hours -- how to learn anything | Josh Kaufman | TEDxCSU'
        WHEN N'VID00000008' THEN N'Big Buck Bunny 60fps 4K - Official Blender Foundation Short Film'
        WHEN N'VID00000009' THEN N'Me at the zoo'
        WHEN N'VID00000010' THEN N'Luis Fonsi - Despacito ft. Daddy Yankee'
        ELSE Title END,
    Description = CASE VideoId
        WHEN N'VID00000001' THEN N'Khóa JavaScript nhập môn thực tế từ kênh freeCodeCamp.org.'
        WHEN N'VID00000002' THEN N'Khóa cơ sở dữ liệu và SQL dành cho người mới từ kênh freeCodeCamp.org.'
        WHEN N'VID00000003' THEN N'Khóa Python cơ bản đầy đủ từ kênh freeCodeCamp.org.'
        WHEN N'VID00000004' THEN N'Hướng dẫn HTML cho người mới bắt đầu từ kênh Traversy Media.'
        WHEN N'VID00000005' THEN N'Bài giảng phương pháp học tập hiệu quả của Marty Lobdell do Pierce College đăng tải.'
        WHEN N'VID00000006' THEN N'Bài nói chuyện TED của Tim Urban về tâm lý trì hoãn.'
        WHEN N'VID00000007' THEN N'Bài nói chuyện TEDx của Josh Kaufman về cách học một kỹ năng mới.'
        WHEN N'VID00000008' THEN N'Phim hoạt hình ngắn chính thức của Blender Foundation, đăng bởi kênh Blender.'
        WHEN N'VID00000009' THEN N'Video Me at the zoo của kênh jawed, một nội dung nổi tiếng trong lịch sử YouTube.'
        WHEN N'VID00000010' THEN N'Video âm nhạc Despacito của Luis Fonsi và Daddy Yankee từ kênh LuisFonsiVEVO.'
        ELSE Description END
WHERE VideoId IN (N'VID00000001', N'VID00000002', N'VID00000003', N'VID00000004', N'VID00000005',
                  N'VID00000006', N'VID00000007', N'VID00000008', N'VID00000009', N'VID00000010');
GO

UPDATE dbo.Videos SET Poster = CASE VideoId
    WHEN N'VID00000001' THEN N'https://i.ytimg.com/vi/PkZNo7MFNFg/hqdefault.jpg'
    WHEN N'VID00000002' THEN N'https://i.ytimg.com/vi/HXV3zeQKqGY/hqdefault.jpg'
    WHEN N'VID00000003' THEN N'https://i.ytimg.com/vi/rfscVS0vtbw/hqdefault.jpg'
    WHEN N'VID00000004' THEN N'https://i.ytimg.com/vi/UB1O30fR-EE/hqdefault.jpg'
    WHEN N'VID00000005' THEN N'https://i.ytimg.com/vi/IlU-zDU6aQ0/hqdefault.jpg'
    WHEN N'VID00000006' THEN N'https://i.ytimg.com/vi/arj7oStGLkU/hqdefault.jpg'
    WHEN N'VID00000007' THEN N'https://i.ytimg.com/vi/5MgBikgcWnY/hqdefault.jpg'
    WHEN N'VID00000008' THEN N'https://i.ytimg.com/vi/aqz-KE-bpKQ/hqdefault.jpg'
    WHEN N'VID00000009' THEN N'https://i.ytimg.com/vi/jNQXAC9IVRw/hqdefault.jpg'
    WHEN N'VID00000010' THEN N'https://i.ytimg.com/vi/kJQP7kiw5Fk/hqdefault.jpg'
    ELSE Poster END
WHERE VideoId IN (N'VID00000001', N'VID00000002', N'VID00000003', N'VID00000004', N'VID00000005',
                  N'VID00000006', N'VID00000007', N'VID00000008', N'VID00000009', N'VID00000010');
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Favorites)
BEGIN
    DECLARE @V1 NVARCHAR(50) = (SELECT TOP 1 VideoId FROM dbo.Videos ORDER BY VideoId);
    DECLARE @V2 NVARCHAR(50) = (SELECT VideoId FROM (SELECT VideoId, ROW_NUMBER() OVER (ORDER BY VideoId) AS rn FROM dbo.Videos) x WHERE rn = 2);
    INSERT dbo.Favorites (LikedDate, VideoId, Username) VALUES
        (CONVERT(DATE, GETDATE()), @V1, N'admin'),
        (CONVERT(DATE, GETDATE()), @V1, N'huuthang'),
        (CONVERT(DATE, GETDATE()), @V2, N'huuthang');
END;

IF NOT EXISTS (SELECT 1 FROM dbo.Shares)
BEGIN
    DECLARE @S1 NVARCHAR(50) = (SELECT TOP 1 VideoId FROM dbo.Videos ORDER BY VideoId);
    DECLARE @S2 NVARCHAR(50) = (SELECT VideoId FROM (SELECT VideoId, ROW_NUMBER() OVER (ORDER BY VideoId) AS rn FROM dbo.Videos) x WHERE rn = 2);
    INSERT dbo.Shares (Emails, SharedDate, Username, VideoId) VALUES
        (N'friend1@example.com', CONVERT(DATE, GETDATE()), N'huuthang', @S1),
        (N'friend2@example.com', CONVERT(DATE, GETDATE()), N'admin', @S1),
        (N'friend3@example.com', CONVERT(DATE, GETDATE()), N'huuthang', @S2);
END;
GO

PRINT N'Database VideoManagement_24133057 đã sẵn sàng.';
PRINT N'Admin: admin / Admin@123';
PRINT N'User: huuthang / User@123';
GO
