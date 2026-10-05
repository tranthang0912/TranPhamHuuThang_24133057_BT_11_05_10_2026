package huuthang.controller;

import huuthang.dao.CommerceDao_24133057;
import huuthang.model.*;
import huuthang.service.UserService_24133057;
import org.junit.jupiter.api.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CommerceControllerTest_24133057 {
    private final Map<String,Object> sessionValues=new HashMap<>(), attributes=new HashMap<>();
    private final Map<String,String> params=new HashMap<>();
    private String path,method,view,location;
    private int status;
    private boolean accountActive;
    private User_24133057 user;
    private FakeDao dao;
    private TestController controller;
    private final HttpSession session=proxy(HttpSession.class,(p,m,a)->switch(m.getName()) {
        case "getAttribute" -> sessionValues.get(a[0]);
        case "setAttribute" -> {sessionValues.put((String)a[0],a[1]);yield null;}
        case "removeAttribute" -> {sessionValues.remove(a[0]);yield null;}
        case "invalidate" -> {sessionValues.clear();yield null;}
        default -> defaultValue(m.getReturnType());
    });
    private final HttpServletRequest request=proxy(HttpServletRequest.class,(p,m,a)->switch(m.getName()) {
        case "getServletPath" -> path; case "getMethod" -> method; case "getContextPath" -> "/exam";
        case "getSession" -> session; case "getParameter" -> params.get(a[0]);
        case "getAttribute" -> attributes.get(a[0]);
        case "setAttribute" -> {attributes.put((String)a[0],a[1]);yield null;}
        case "getRequestDispatcher" -> {String target=(String)a[0];yield proxy(RequestDispatcher.class,(x,n,b)->{if(n.getName().equals("forward"))view=target;return null;});}
        default -> defaultValue(m.getReturnType());
    });
    private final HttpServletResponse response=proxy(HttpServletResponse.class,(p,m,a)->switch(m.getName()) {
        case "setStatus","sendError" -> {status=(int)a[0];yield null;}
        case "sendRedirect" -> {location=(String)a[0];status=302;yield null;}
        default -> defaultValue(m.getReturnType());
    });
    @BeforeEach void setup()throws Exception {
        status=200; accountActive=true; sessionValues.clear();params.clear();attributes.clear();view=null;location=null;
        user=new User_24133057();user.setUsername("buyer");user.setActive(true);user.setFullname("Nguyễn Văn An");user.setPhone("0912345678");
        sessionValues.put("currentUser",user);sessionValues.put("commerceCsrf","csrf-fixture");
        dao=new FakeDao();controller=new TestController();
        inject("dao",dao); inject("users",proxy(UserService_24133057.class,(p,m,a)->m.getName().equals("findByUsername")
                ? (accountActive?Optional.of(user):Optional.empty()):null));
    }
    @Test void anonymousHistoryRequiresLogin()throws Exception {
        sessionValues.remove("currentUser");get("/orders");assertEquals(302,status);assertEquals("/exam/auth/login",location);assertEquals(0,dao.queries);
    }
    @Test void blockedAccountCannotMutateItsCart()throws Exception {
        accountActive=false;validCsrf();params.put("action","clear");post("/cart");assertEquals(302,status);assertEquals(0,dao.mutations);assertNull(sessionValues.get("currentUser"));
    }
    @Test void rejectsMissingOrForgedCsrfBeforeMutation()throws Exception {
        params.put("action","clear");post("/cart");assertEquals(403,status);assertEquals(0,dao.mutations);
        params.put("csrfToken","forged");post("/cart");assertEquals(403,status);assertEquals(0,dao.mutations);
    }
    @Test void rejectsMalformedQuantityInsteadOfUsingADefault()throws Exception {
        validCsrf();params.put("action","add");params.put("productId","1");params.put("quantity","1.2");post("/cart");
        assertEquals(400,status);assertEquals(0,dao.mutations);assertTrue(view.endsWith("cart_24133057.jsp"));
    }
    @Test void validCartMutationUsesSessionOwnerAndRedirects()throws Exception {
        validCsrf();params.put("action","add");params.put("productId","1");params.put("quantity","2");params.put("username","victim");post("/cart");
        assertEquals(1,dao.mutations);assertEquals("buyer",dao.owner);assertEquals("/exam/cart",location);
    }
    @Test void getCannotCancelOrders()throws Exception {get("/orders/cancel");assertEquals(405,status);assertEquals(0,dao.mutations);}
    @Test void unknownFilterIsRejectedBeforeQuery()throws Exception {
        params.put("status","INVALID");get("/orders");assertEquals(400,status);assertEquals(0,dao.queries);
    }
    @Test void orderDetailQueriesOnlySignedInOwnerAndHidesMissingOrder()throws Exception {
        params.put("id","999");params.put("username","victim");get("/orders/detail");assertEquals("buyer",dao.owner);assertEquals(404,status);
    }
    @Test void forgedCheckoutTokenDoesNotPlaceOrder()throws Exception {
        validCsrf();validForm();params.put("checkoutToken",UUID.randomUUID().toString());post("/checkout");assertEquals(400,status);assertEquals(0,dao.mutations);
    }
    @Test void invalidCheckoutShowsFieldErrorsAndRetainsEnteredValues()throws Exception {
        get("/checkout");String token=(String)attributes.get("checkoutToken");attributes.clear();validCsrf();validForm();
        params.put("checkoutToken",token);params.put("recipient","<script>");params.put("phone","abc");params.put("address","12");post("/checkout");
        assertEquals(400,status);assertEquals(0,dao.mutations);assertEquals(3,((Map<?,?>)attributes.get("errors")).size());
        assertEquals("<script>",((CheckoutDetails_24133057)attributes.get("form")).getRecipient());assertTrue(view.endsWith("checkout_24133057.jsp"));
    }
    @Test void checkoutUsesServerFingerprintAndIgnoresClientPrices()throws Exception {
        get("/checkout");String token=(String)attributes.get("checkoutToken");validCsrf();validForm();params.put("checkoutToken",token);
        params.put("total","1");params.put("price","0");params.put("username","victim");post("/checkout");
        assertEquals(1,dao.mutations);assertEquals("buyer",dao.owner);assertEquals(dao.cart.fingerprint(),dao.fingerprint);assertEquals("/exam/orders/detail?id=42",location);
    }
    @Test void unavailableDatabaseShowsControlledError()throws Exception {
        dao.unavailable=true;get("/cart");assertEquals(503,status);assertTrue(view.endsWith("shop-error_24133057.jsp"));
    }
    private void validCsrf(){params.put("csrfToken","csrf-fixture");}
    private void validForm(){params.put("recipient","Nguyễn Văn An");params.put("phone","0912345678");params.put("address","12 Nguyễn Văn Bảo, TP Hồ Chí Minh");params.put("paymentMethod","COD");}
    private void get(String route)throws Exception {path=route;method="GET";controller.doGet(request,response);}
    private void post(String route)throws Exception {path=route;method="POST";controller.doPost(request,response);}
    private void inject(String name,Object value)throws Exception{Field f=CommerceController_24133057.class.getDeclaredField(name);f.setAccessible(true);f.set(controller,value);}
    @SuppressWarnings("unchecked") private static <T>T proxy(Class<T> type,InvocationHandler handler){return (T)Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},handler);}
    private static Object defaultValue(Class<?> type){if(type==boolean.class)return false;if(type==int.class)return 0;if(type==long.class)return 0L;return null;}
    private static class TestController extends CommerceController_24133057 {
        @Override public ServletContext getServletContext(){return proxy(ServletContext.class,(p,m,a)->defaultValue(m.getReturnType()));}
    }
    private static class FakeDao extends CommerceDao_24133057 {
        int mutations,queries;String owner,fingerprint;boolean unavailable;
        final Cart_24133057 cart=new Cart_24133057(List.of(new CartLine_24133057(new Product_24133057(1,"VID1","Video học tập","","",new BigDecimal("200000"),5,true),2)));
        @Override public Cart_24133057 cart(String username)throws SQLException{queries++;owner=username;if(unavailable)throw new SQLException("offline");return cart;}
        @Override public void changeCart(String username,int id,int quantity,boolean add){owner=username;mutations++;}
        @Override public void remove(String username,Integer id){owner=username;mutations++;}
        @Override public void cancel(String username,long id){owner=username;mutations++;}
        @Override public Optional<Order_24133057> order(String username,long id){queries++;owner=username;return Optional.empty();}
        @Override public long checkout(String username,String token,String digest,CheckoutDetails_24133057 details){owner=username;fingerprint=digest;mutations++;return 42;}
    }
}
