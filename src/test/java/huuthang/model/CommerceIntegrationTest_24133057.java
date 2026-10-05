package huuthang.model;

import huuthang.config.DatabaseConnection_24133057;
import huuthang.dao.CommerceDao_24133057;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Opt in with RUN_DATABASE_TESTS=true after applying database/commerce.sql. */
@EnabledIfEnvironmentVariable(named="RUN_DATABASE_TESTS", matches="true")
class CommerceIntegrationTest_24133057 {
    private final CommerceDao_24133057 dao = new CommerceDao_24133057();
    private String username, otherUser, videoId;
    private int productId;
    private final CheckoutDetails_24133057 details = new CheckoutDetails_24133057("Nguyễn Văn An", "0912345678", "12 Nguyễn Văn Bảo, TP Hồ Chí Minh", "", "COD");
    @BeforeEach void setup() throws Exception {
        username = "test_" + UUID.randomUUID().toString().replace("-", ""); otherUser = username + "_b";
        try (Connection c = DatabaseConnection_24133057.getConnection()) {
            for (String name : List.of(username,otherUser)) execute(c,"INSERT dbo.Users(Username,Active,Fullname) VALUES(?,1,N'Kiểm thử')",name);
            try (PreparedStatement s = c.prepareStatement("INSERT dbo.Videos(Title,Active,CategoryId) OUTPUT inserted.VideoId VALUES(N'Video kiểm thử',1,(SELECT TOP 1 CategoryId FROM dbo.Category WHERE Status=1))"); ResultSet rs=s.executeQuery()) {
                assertTrue(rs.next()); videoId=rs.getNString(1);
            }
            try (PreparedStatement s=c.prepareStatement("INSERT dbo.Products(VideoId,Price,Stock) OUTPUT inserted.ProductId VALUES(?,200000,5)")) {
                s.setNString(1,videoId); try(ResultSet rs=s.executeQuery()){ assertTrue(rs.next()); productId=rs.getInt(1); }
            }
        }
    }
    @AfterEach void cleanup() throws Exception {
        try (Connection c=DatabaseConnection_24133057.getConnection()) {
            c.setAutoCommit(false);
            try {
                for (String name:List.of(username,otherUser)) {
                    execute(c,"DELETE dbo.ActivationEmailOutbox WHERE OrderId IN (SELECT OrderId FROM dbo.Orders WHERE Username=?)",name);
                    execute(c,"DELETE dbo.VideoActivationCodes WHERE OrderId IN (SELECT OrderId FROM dbo.Orders WHERE Username=?)",name);
                    execute(c,"DELETE dbo.OrderStatusHistory WHERE OrderId IN (SELECT OrderId FROM dbo.Orders WHERE Username=?)",name);
                    execute(c,"DELETE dbo.OrderItems WHERE OrderId IN (SELECT OrderId FROM dbo.Orders WHERE Username=?)",name);
                    execute(c,"DELETE dbo.Orders WHERE Username=?",name); execute(c,"DELETE dbo.CartItems WHERE Username=?",name);
                }
                execute(c,"DELETE dbo.Favorites WHERE VideoId=?",videoId); execute(c,"DELETE dbo.Shares WHERE VideoId=?",videoId);
                execute(c,"DELETE dbo.Products WHERE ProductId=?",productId); execute(c,"DELETE dbo.Videos WHERE VideoId=?",videoId);
                execute(c,"DELETE dbo.Users WHERE Username=?",username); execute(c,"DELETE dbo.Users WHERE Username=?",otherUser); c.commit();
            } catch(Exception e) { c.rollback(); throw e; }
        }
    }
    @Test void cartAddsUpdatesRemovesAndEnforcesStockWithoutChangingOnError() throws Exception {
        dao.changeCart(username,productId,2,true); dao.changeCart(username,productId,1,true);
        assertEquals(3,dao.cart(username).getQuantity());
        assertThrows(IllegalArgumentException.class,()->dao.changeCart(username,productId,3,true));
        assertEquals(3,dao.cart(username).getQuantity());
        assertThrows(IllegalArgumentException.class,()->dao.changeCart(otherUser,productId,2,false));
        dao.changeCart(username,productId,5,false); assertEquals(5,dao.cart(username).getQuantity());
        dao.remove(username,productId); assertTrue(dao.cart(username).isEmpty());
    }
    @Test void checkoutIsAtomicIdempotentAndPrivate() throws Exception {
        dao.changeCart(username,productId,2,true); String token=UUID.randomUUID().toString(), digest=dao.cart(username).fingerprint();
        long id=dao.checkout(username,token,digest,details);
        assertEquals(id,dao.checkout(username,token,digest,details)); assertEquals(3,stock()); assertTrue(dao.cart(username).isEmpty());
        Order_24133057 order=dao.order(username,id).orElseThrow();
        assertEquals(OrderStatus_24133057.NEW,order.getStatus()); assertEquals("PENDING",order.getPaymentStatus());
        assertEquals(400000,order.getTotal().intValueExact()); assertEquals(1,order.getEvents().size());
        assertTrue(dao.order(otherUser,id).isEmpty()); assertThrows(IllegalArgumentException.class,()->dao.cancel(otherUser,id));
    }
    @Test void staleCartAndEmptyCartDoNotCreateAnOrderOrDecrementStock() throws Exception {
        assertThrows(IllegalArgumentException.class,()->dao.checkout(username,UUID.randomUUID().toString(),"invalid",details));
        dao.changeCart(username,productId,1,true); String digest=dao.cart(username).fingerprint(); dao.changeCart(username,productId,2,false);
        assertThrows(IllegalArgumentException.class,()->dao.checkout(username,UUID.randomUUID().toString(),digest,details));
        assertEquals(5,stock()); assertEquals(2,dao.cart(username).getQuantity()); assertEquals(0,dao.counts(username).get("ALL"));
    }
    @Test void priceChangesRequireReconfirmationAndPastOrdersKeepOriginalPrice() throws Exception {
        dao.changeCart(username,productId,1,true); String digest=dao.cart(username).fingerprint();
        try(Connection c=DatabaseConnection_24133057.getConnection()){execute(c,"UPDATE dbo.Products SET Price=300000 WHERE ProductId=?",productId);}
        assertThrows(IllegalArgumentException.class,()->dao.checkout(username,UUID.randomUUID().toString(),digest,details));
        long id=place(username);
        try(Connection c=DatabaseConnection_24133057.getConnection()){execute(c,"UPDATE dbo.Products SET Price=400000 WHERE ProductId=?",productId);}
        assertEquals(300000,dao.order(username,id).orElseThrow().getItems().get(0).getUnitPrice().intValueExact());
    }
    @Test void cancellationRestoresStockOnlyOnceAndRejectsReopening() throws Exception {
        dao.changeCart(username,productId,2,true); long id=place(username); dao.cancel(username,id);
        assertEquals(5,stock()); assertEquals("VOID",dao.order(username,id).orElseThrow().getPaymentStatus());
        assertThrows(IllegalArgumentException.class,()->dao.cancel(username,id)); assertEquals(5,stock());
        assertThrows(SQLException.class,()->status(id,"NEW"));
        assertEquals(1,dao.orders(username,OrderStatus_24133057.CANCELLED,1).size());
    }
    @Test void databaseStatusUpdatesChangeFiltersTimelineCodPaymentAndReturnStock() throws Exception {
        dao.changeCart(username,productId,2,true); long id=place(username);
        assertThrows(SQLException.class,()->status(id,"DELIVERED"));
        for(String state:List.of("CONFIRMED","PREPARING","SHIPPING","OUT_FOR_DELIVERY","DELIVERED")) {
            status(id,state); assertEquals(1,dao.orders(username,OrderStatus_24133057.valueOf(state),1).size());
            assertEquals(0,dao.orders(username,OrderStatus_24133057.NEW,1).size());
        }
        assertEquals("PAID",dao.order(username,id).orElseThrow().getPaymentStatus());
        status(id,"RETURNED"); assertEquals(5,stock());
        Order_24133057 order=dao.order(username,id).orElseThrow(); assertEquals("REFUNDED",order.getPaymentStatus()); assertEquals(7,order.getEvents().size());
        status(id,"RETURNED"); assertEquals(5,stock()); assertEquals(7,dao.order(username,id).orElseThrow().getEvents().size());
        assertThrows(SQLException.class,()->status(id,"CONFIRMED"));
    }
    @Test void concurrentCustomersCannotOversell() throws Exception {
        dao.changeCart(username,productId,4,true); dao.changeCart(otherUser,productId,4,true);
        String digest=dao.cart(username).fingerprint(); ExecutorService pool=Executors.newFixedThreadPool(2);
        CountDownLatch start=new CountDownLatch(1);
        try {
            List<Future<Boolean>> futures=new ArrayList<>();
            for(String name:List.of(username,otherUser)) futures.add(pool.submit(()->{
                start.await(); try{dao.checkout(name,UUID.randomUUID().toString(),digest,details);return true;}
                catch(IllegalArgumentException e){return false;}
            }));
            start.countDown(); int successful=0; for(Future<Boolean> future:futures) if(future.get(30,TimeUnit.SECONDS)) successful++;
            assertEquals(1,successful); assertEquals(1,stock());
            assertEquals(1,dao.counts(username).get("ALL")+dao.counts(otherUser).get("ALL"));
        } finally {pool.shutdownNow();}
    }
    @Test void concurrentRetriesCreateOneOrder() throws Exception {
        dao.changeCart(username,productId,2,true); String digest=dao.cart(username).fingerprint(), token=UUID.randomUUID().toString();
        ExecutorService pool=Executors.newFixedThreadPool(2);
        try {
            Future<Long> first=pool.submit(()->dao.checkout(username,token,digest,details));
            Future<Long> second=pool.submit(()->dao.checkout(username,token,digest,details));
            assertEquals(first.get(30,TimeUnit.SECONDS),second.get(30,TimeUnit.SECONDS));
            assertEquals(3,stock()); assertEquals(1,dao.counts(username).get("ALL"));
        } finally {pool.shutdownNow();}
    }
    private long place(String name)throws Exception {return dao.checkout(name,UUID.randomUUID().toString(),dao.cart(name).fingerprint(),details);}
    @Test void favoriteIsIdempotentAndRemovingOnlyAffectsOwnFavorite() throws Exception {
        var interaction=new huuthang.dao.VideoInteractionDao_24133057();
        interaction.favorite(username,videoId,true); interaction.favorite(username,videoId,true); interaction.favorite(otherUser,videoId,true);
        assertEquals(2,new huuthang.dao.impl.VideoDaoImpl_24133057().findById(videoId).orElseThrow().getLikeCount());
        assertEquals(1,interaction.favorites(username).size());
        interaction.favorite(username,videoId,false); interaction.favorite(username,videoId,false);
        assertFalse(interaction.liked(username,videoId)); assertTrue(interaction.liked(otherUser,videoId));
        assertEquals(1,new huuthang.dao.impl.VideoDaoImpl_24133057().findById(videoId).orElseThrow().getLikeCount());
    }
    @Test void repeatedShareCountsOncePerAccountPerDayAndInactiveAccountCannotLike() throws Exception {
        var interaction=new huuthang.dao.VideoInteractionDao_24133057();
        interaction.share(username,videoId); interaction.share(username,videoId); interaction.share(otherUser,videoId);
        assertEquals(2,new huuthang.dao.impl.VideoDaoImpl_24133057().findById(videoId).orElseThrow().getShareCount());
        try(Connection c=DatabaseConnection_24133057.getConnection()){execute(c,"UPDATE dbo.Users SET Active=0 WHERE Username=?",username);}
        assertThrows(IllegalArgumentException.class,()->interaction.favorite(username,videoId,true));
    }
    @Test void codesAreIssuedOnlyAfterCodDeliveryOncePerPurchasedUnit() throws Exception {
        var activation=new huuthang.dao.ActivationDao_24133057(); dao.changeCart(username,productId,2,true); long id=place(username);
        assertTrue(activation.orderCodes(username,id).isEmpty()); assertFalse(activation.unlocked(username,videoId));
        for(String state:List.of("CONFIRMED","PREPARING","SHIPPING","OUT_FOR_DELIVERY")) {status(id,state); assertTrue(activation.orderCodes(username,id).isEmpty());}
        status(id,"DELIVERED"); status(id,"DELIVERED");
        assertEquals(2,activation.orderCodes(username,id).size()); assertTrue(activation.orderCodes(otherUser,id).isEmpty());
        assertNotEquals(activation.orderCodes(username,id).get(0).getCode(),activation.orderCodes(username,id).get(1).getCode());
    }
    @Test void codeIsSingleUseRetrySafeAndDuplicateVideoDoesNotConsumeSecondCode() throws Exception {
        var activation=new huuthang.dao.ActivationDao_24133057(); dao.changeCart(username,productId,2,true); long id=place(username); deliver(id);
        var codes=activation.orderCodes(username,id); String first=codes.get(0).getCode(),second=codes.get(1).getCode();
        assertEquals(videoId,activation.redeem(username,first.toLowerCase(Locale.ROOT))); assertEquals(videoId,activation.redeem(username,first));
        assertThrows(IllegalArgumentException.class,()->activation.redeem(otherUser,first));
        assertThrows(IllegalArgumentException.class,()->activation.redeem(username,second));
        assertEquals(videoId,activation.redeem(otherUser,second)); assertTrue(activation.unlocked(otherUser,videoId));
        assertEquals(1,activation.library(username).size()); assertEquals(1,activation.library(otherUser).size());
    }
    @Test void returningPaidOrderRevokesCodesAndPreviouslyActivatedAccess() throws Exception {
        var activation=new huuthang.dao.ActivationDao_24133057(); dao.changeCart(username,productId,1,true); long id=place(username); deliver(id);
        String code=activation.orderCodes(username,id).get(0).getCode(); activation.redeem(otherUser,code); status(id,"RETURNED");
        assertFalse(activation.unlocked(otherUser,videoId)); assertTrue(activation.library(otherUser).isEmpty());
        assertEquals("REVOKED",activation.orderCodes(username,id).get(0).getStatus());
        assertThrows(IllegalArgumentException.class,()->activation.redeem(otherUser,code));
    }
    @Test void emailQueueAppearsAtPaymentAndIsCancelledWhenOrderIsReturned() throws Exception {
        dao.changeCart(username,productId,1,true); long id=place(username);
        // Keep the fixture outbox uncommitted so a running application cannot send a test email.
        try(Connection c=DatabaseConnection_24133057.getConnection()) {
            c.setAutoCommit(false);
            try {
                execute(c,"UPDATE dbo.Users SET Email='activation-fixture@example.invalid' WHERE Username=?",username);
                for(String state:List.of("CONFIRMED","PREPARING","SHIPPING","OUT_FOR_DELIVERY","DELIVERED"))execute(c,"UPDATE dbo.Orders SET Status=? WHERE OrderId=?",state,id);
                try(PreparedStatement s=c.prepareStatement("SELECT Status FROM dbo.ActivationEmailOutbox WHERE OrderId=?")){s.setLong(1,id);try(ResultSet rs=s.executeQuery()){assertTrue(rs.next());assertEquals("PENDING",rs.getString(1));}}
                execute(c,"UPDATE dbo.Orders SET Status='RETURNED' WHERE OrderId=?",id);
                try(PreparedStatement s=c.prepareStatement("SELECT Status FROM dbo.ActivationEmailOutbox WHERE OrderId=?")){s.setLong(1,id);try(ResultSet rs=s.executeQuery()){assertTrue(rs.next());assertEquals("CANCELLED",rs.getString(1));}}
            } finally {c.rollback();}
        }
    }
    @Test void adminCanConfirmAndCollectCodWithoutDuplicatingCodes() throws Exception {
        try (Connection c=DatabaseConnection_24133057.getConnection()) { execute(c,"UPDATE dbo.Users SET Admin=1 WHERE Username=?",otherUser); }
        dao.changeCart(username,productId,2,true); long id=place(username);
        var activation=new huuthang.dao.ActivationDao_24133057();
        dao.adminProcess(otherUser,id,"confirm"); dao.adminProcess(otherUser,id,"confirm");
        assertEquals(OrderStatus_24133057.CONFIRMED,dao.order(username,id).orElseThrow().getStatus());
        assertTrue(activation.orderCodes(username,id).isEmpty());
        dao.adminProcess(otherUser,id,"collect-cod"); dao.adminProcess(otherUser,id,"collect-cod");
        var order=dao.adminOrder(id).orElseThrow();
        assertEquals(username,order.getUsername()); assertEquals(OrderStatus_24133057.DELIVERED,order.getStatus());
        assertEquals("PAID",order.getPaymentStatus()); assertEquals(6,order.getEvents().size());
        assertEquals(2,activation.orderCodes(username,id).size()); assertEquals(3,stock());
        assertEquals(videoId,activation.redeem(username,activation.orderCodes(username,id).get(0).getCode()));
        assertTrue(activation.unlocked(username,videoId));
    }
    @Test void adminProcessingRejectsWrongRoleUnknownActionsAndUnconfirmedCod() throws Exception {
        dao.changeCart(username,productId,1,true); long id=place(username);
        assertThrows(IllegalArgumentException.class,()->dao.adminProcess(username,id,"confirm"));
        try (Connection c=DatabaseConnection_24133057.getConnection()) { execute(c,"UPDATE dbo.Users SET Admin=1 WHERE Username=?",otherUser); }
        assertThrows(IllegalArgumentException.class,()->dao.adminProcess(otherUser,id,"collect-cod"));
        assertThrows(IllegalArgumentException.class,()->dao.adminProcess(otherUser,id,"invalid"));
        assertThrows(IllegalArgumentException.class,()->dao.adminProcess(otherUser,Long.MAX_VALUE,"confirm"));
        assertEquals(OrderStatus_24133057.NEW,dao.order(username,id).orElseThrow().getStatus());
        assertTrue(new huuthang.dao.ActivationDao_24133057().orderCodes(username,id).isEmpty());
        try (Connection c=DatabaseConnection_24133057.getConnection()) { execute(c,"UPDATE dbo.Users SET Active=0 WHERE Username=?",otherUser); }
        assertThrows(IllegalArgumentException.class,()->dao.adminProcess(otherUser,id,"confirm"));
    }
    @Test void cancelledOrdersCannotBeConfirmedOrIssuedCodesByAdmin() throws Exception {
        try (Connection c=DatabaseConnection_24133057.getConnection()) { execute(c,"UPDATE dbo.Users SET Admin=1 WHERE Username=?",otherUser); }
        dao.changeCart(username,productId,1,true); long id=place(username); dao.cancel(username,id);
        assertThrows(IllegalArgumentException.class,()->dao.adminProcess(otherUser,id,"confirm"));
        assertThrows(IllegalArgumentException.class,()->dao.adminProcess(otherUser,id,"collect-cod"));
        assertEquals(5,stock()); assertTrue(new huuthang.dao.ActivationDao_24133057().orderCodes(username,id).isEmpty());
    }
    @Test void adminUpdatesAllStatesFiltersAndRevokesReturnedCodes() throws Exception {
        try (Connection c=DatabaseConnection_24133057.getConnection()) { execute(c,"UPDATE dbo.Users SET Admin=1 WHERE Username=?",otherUser); }
        dao.changeCart(username,productId,1,true); long id=place(username);
        assertThrows(IllegalArgumentException.class,()->dao.adminProcess(otherUser,id,"DELIVERED"));
        for (String state:List.of("CONFIRMED","PREPARING","SHIPPING","OUT_FOR_DELIVERY","DELIVERED")) {
            dao.adminProcess(otherUser,id,state);
            assertEquals(state,dao.adminOrder(id).orElseThrow().getStatus().name());
            assertTrue(dao.adminOrders(OrderStatus_24133057.valueOf(state)).stream().anyMatch(o->o.getOrderId()==id));
        }
        var activation=new huuthang.dao.ActivationDao_24133057();
        activation.redeem(username,activation.orderCodes(username,id).get(0).getCode());
        dao.adminProcess(otherUser,id,"RETURNED");
        assertFalse(activation.unlocked(username,videoId)); assertEquals(5,stock());
        assertEquals("REFUNDED",dao.adminOrder(id).orElseThrow().getPaymentStatus());
        assertThrows(IllegalArgumentException.class,()->dao.adminProcess(otherUser,id,"collect-cod"));
        dao.changeCart(username,productId,1,true); long cancelled=place(username);
        dao.adminProcess(otherUser,cancelled,"CANCELLED");
        assertEquals("VOID",dao.adminOrder(cancelled).orElseThrow().getPaymentStatus()); assertEquals(5,stock());
    }
    private void deliver(long id)throws Exception {for(String state:List.of("CONFIRMED","PREPARING","SHIPPING","OUT_FOR_DELIVERY","DELIVERED"))status(id,state);}
    private void status(long id,String status)throws Exception {try(Connection c=DatabaseConnection_24133057.getConnection()){execute(c,"UPDATE dbo.Orders SET Status=? WHERE OrderId=?",status,id);}}
    private int stock()throws Exception {try(Connection c=DatabaseConnection_24133057.getConnection();PreparedStatement s=c.prepareStatement("SELECT Stock FROM dbo.Products WHERE ProductId=?")){s.setInt(1,productId);try(ResultSet rs=s.executeQuery()){rs.next();return rs.getInt(1);}}}
    private void execute(Connection c,String sql,Object...args)throws SQLException {try(PreparedStatement s=c.prepareStatement(sql)){for(int i=0;i<args.length;i++)s.setObject(i+1,args[i]);s.executeUpdate();}}
}
