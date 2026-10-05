package huuthang.service.impl;

import huuthang.dao.UserDao_24133057;
import huuthang.model.Page_24133057;
import huuthang.model.User_24133057;
import huuthang.service.UserService_24133057;
import huuthang.util.PasswordUtil_24133057;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserServiceImpl_24133057 implements UserService_24133057 {
    private static final int USERS_PER_PAGE = 6;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9._-]{3,50}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9\\-]*[A-Za-z0-9])?)+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:0|\\+84)[35789][0-9]{8}$");
    private static final Pattern FULLNAME_PATTERN = Pattern.compile("[\\p{L}\\p{M} .'-]{2,50}");

    private final UserDao_24133057 userDao;

    public UserServiceImpl_24133057(UserDao_24133057 userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<User_24133057> authenticate(String username, String password) throws SQLException {
        if (username == null || password == null) {
            return Optional.empty();
        }
        Optional<User_24133057> found = userDao.findByUsername(username.trim());
        if (found.isEmpty() || !found.get().isActive()
                || !PasswordUtil_24133057.matches(password, found.get().getPassword())) {
            return Optional.empty();
        }
        return found;
    }

    @Override
    public void register(User_24133057 user, String rawPassword) throws SQLException {
        validate(user, rawPassword, true);
        if (userDao.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }
        if (userDao.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }
        user.setPassword(PasswordUtil_24133057.hash(rawPassword));
        user.setAdmin(false);
        user.setActive(false);
        if (user.getImages() == null || user.getImages().isBlank()) {
            user.setImages("/assets/images/avatar-user.svg");
        }
        userDao.create(user);
    }

    @Override
    public void activate(String username) throws SQLException {
        userDao.activate(username);
    }

    @Override
    public Page_24133057<User_24133057> getUsers(int page) throws SQLException {
        long total = userDao.count();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / USERS_PER_PAGE));
        int safePage = Math.min(Math.max(1, page), totalPages);
        List<User_24133057> users = userDao.findPage((safePage - 1) * USERS_PER_PAGE, USERS_PER_PAGE);
        return new Page_24133057<>(users, safePage, USERS_PER_PAGE, total);
    }

    @Override
    public Optional<User_24133057> findByUsername(String username) throws SQLException {
        return userDao.findByUsername(username);
    }

    @Override
    public void saveByAdmin(User_24133057 user, String rawPassword, boolean creating) throws SQLException {
        validate(user, rawPassword, creating);
        if (creating) {
            if (userDao.findByUsername(user.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
            }
            if (userDao.findByEmail(user.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email đã được sử dụng.");
            }
            user.setPassword(PasswordUtil_24133057.hash(rawPassword));
            userDao.create(user);
            return;
        }

        User_24133057 current = userDao.findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));
        if (userDao.findByEmail(user.getEmail()).filter(other -> !other.getUsername().equalsIgnoreCase(user.getUsername())).isPresent()) {
            throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác.");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            user.setPassword(current.getPassword());
        } else {
            user.setPassword(PasswordUtil_24133057.hash(rawPassword));
        }
        userDao.update(user);
    }

    @Override
    public void delete(String username) throws SQLException {
        userDao.delete(username);
    }

    private void validate(User_24133057 user, String password, boolean passwordRequired) {
        user.setUsername(trim(user.getUsername()));
        user.setFullname(trim(user.getFullname()));
        user.setEmail(trim(user.getEmail()));
        user.setPhone(trim(user.getPhone()));
        user.setImages(trim(user.getImages()));

        if (user.getUsername() == null || !USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            throw new IllegalArgumentException("Tên đăng nhập phải có 3–50 ký tự: chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.");
        }
        if ((passwordRequired && (password == null || password.isBlank()))
                || (password != null && !password.isBlank() && (password.length() < 6 || password.length() > 128))) {
            throw new IllegalArgumentException("Mật khẩu phải có từ 6 đến 128 ký tự.");
        }
        if (user.getFullname() == null || !FULLNAME_PATTERN.matcher(user.getFullname()).matches()) {
            throw new IllegalArgumentException("Họ tên phải có 2–50 ký tự và không chứa số hoặc ký tự đặc biệt.");
        }
        if (user.getEmail() == null || user.getEmail().length() > 150
                || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
        if (user.getPhone() != null && !PHONE_PATTERN.matcher(user.getPhone()).matches()) {
            throw new IllegalArgumentException("Số di động Việt Nam không hợp lệ. Ví dụ: 0912345678 hoặc +84912345678.");
        }
        if (user.getImages() != null && user.getImages().length() > 500) {
            throw new IllegalArgumentException("Đường dẫn ảnh tối đa 500 ký tự.");
        }
    }

    private String trim(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
