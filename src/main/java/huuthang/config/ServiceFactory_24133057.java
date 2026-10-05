package huuthang.config;

import huuthang.dao.CategoryDao_24133057;
import huuthang.dao.UserDao_24133057;
import huuthang.dao.VideoDao_24133057;
import huuthang.dao.impl.CategoryDaoImpl_24133057;
import huuthang.dao.impl.UserDaoImpl_24133057;
import huuthang.dao.impl.VideoDaoImpl_24133057;
import huuthang.service.EmailService_24133057;
import huuthang.service.UserService_24133057;
import huuthang.service.VideoService_24133057;
import huuthang.service.impl.EmailServiceImpl_24133057;
import huuthang.service.impl.UserServiceImpl_24133057;
import huuthang.service.impl.VideoServiceImpl_24133057;

public final class ServiceFactory_24133057 {
    private ServiceFactory_24133057() {
    }

    public static UserService_24133057 userService() {
        UserDao_24133057 userDao = new UserDaoImpl_24133057();
        return new UserServiceImpl_24133057(userDao);
    }

    public static VideoService_24133057 videoService() {
        VideoDao_24133057 videoDao = new VideoDaoImpl_24133057();
        CategoryDao_24133057 categoryDao = new CategoryDaoImpl_24133057();
        return new VideoServiceImpl_24133057(videoDao, categoryDao);
    }

    public static EmailService_24133057 emailService() {
        return new EmailServiceImpl_24133057();
    }
}
