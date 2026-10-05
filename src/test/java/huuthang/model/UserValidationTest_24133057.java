package huuthang.model;

import huuthang.dao.UserDao_24133057;
import huuthang.service.impl.UserServiceImpl_24133057;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest_24133057 {
    private User_24133057 form() {
        User_24133057 user=new User_24133057();user.setUsername("student");user.setFullname("Nguyễn Văn An");user.setEmail("an@example.invalid");user.setPhone("0912345678");return user;
    }
    @Test void invalidNameOrMobileCannotCreateAnAccount() {
        FakeDao dao=new FakeDao();var service=new UserServiceImpl_24133057(dao);var user=form();user.setFullname("<script>123</script>");
        assertThrows(IllegalArgumentException.class,()->service.register(user,"validPassword"));
        user.setFullname("Nguyễn Văn An");user.setPhone("++++++++++");
        assertThrows(IllegalArgumentException.class,()->service.register(user,"validPassword"));assertFalse(dao.saved);
        user.setPhone("0912345678");user.setEmail("<script>@example.invalid");
        assertThrows(IllegalArgumentException.class,()->service.register(user,"validPassword"));assertFalse(dao.saved);
    }
    @Test void editingAnAccountAlsoValidatesNewPassword() {
        FakeDao dao=new FakeDao();dao.current=form();var service=new UserServiceImpl_24133057(dao);
        assertThrows(IllegalArgumentException.class,()->service.saveByAdmin(form(),"123",false));
        assertThrows(IllegalArgumentException.class,()->service.saveByAdmin(form(),"a".repeat(129),false));assertFalse(dao.saved);
    }
    @Test void duplicateEmailCannotOverwriteAnotherAccount() {
        FakeDao dao=new FakeDao();dao.current=form();dao.emailOwner=form();dao.emailOwner.setUsername("other");var service=new UserServiceImpl_24133057(dao);
        assertThrows(IllegalArgumentException.class,()->service.saveByAdmin(form(),"",false));assertFalse(dao.saved);
    }
    @Test void keepingOwnEmailAndBlankPasswordPreservesExistingPassword() throws Exception {
        FakeDao dao=new FakeDao();dao.current=form();dao.current.setPassword("existing-hash");dao.emailOwner=dao.current;
        var edited=form();edited.setPhone("+84912345678");new UserServiceImpl_24133057(dao).saveByAdmin(edited,"",false);
        assertTrue(dao.saved);assertEquals("existing-hash",edited.getPassword());
    }
    private static class FakeDao implements UserDao_24133057 {
        User_24133057 current,emailOwner;boolean saved;
        public Optional<User_24133057> findByUsername(String name){return Optional.ofNullable(current);}
        public Optional<User_24133057> findByEmail(String email){return Optional.ofNullable(emailOwner);}
        public List<User_24133057> findPage(int offset,int limit){return List.of();}public long count(){return 0;}
        public void create(User_24133057 user){saved=true;}public void update(User_24133057 user){saved=true;}
        public void activate(String username){}public void delete(String username){}
    }
}
