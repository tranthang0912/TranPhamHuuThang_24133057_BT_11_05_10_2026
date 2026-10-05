package huuthang.model;

import huuthang.util.CommerceValidation_24133057;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CommerceValidationTest_24133057 {
    @ParameterizedTest @NullSource @ValueSource(strings={"", "0", "-1", "100", "1.5", "1e2", "NaN", " 2 ", "999999999999999999999", "<script>"})
    void rejectsMalformedAndOutOfRangeQuantity(String value) {
        assertThrows(IllegalArgumentException.class, () -> CommerceValidation_24133057.quantity(value));
    }
    @Test void acceptsQuantityBoundariesAndRejectsInvalidIds() {
        assertEquals(1, CommerceValidation_24133057.quantity("1"));
        assertEquals(99, CommerceValidation_24133057.quantity("99"));
        assertThrows(IllegalArgumentException.class, () -> CommerceValidation_24133057.productId("2147483648"));
        assertThrows(IllegalArgumentException.class, () -> CommerceValidation_24133057.orderId("9223372036854775808"));
        assertThrows(IllegalArgumentException.class, () -> CommerceValidation_24133057.page("0"));
    }
    @Test void acceptsVietnameseRecipientAndNormalizedPhoneAndAddress() {
        CheckoutDetails_24133057 form = new CheckoutDetails_24133057("  Trần Phạm Hữu Thắng  ",
                "+84 912 345 678", "12 Nguyễn Văn Bảo\nPhường Hạnh Thông, TP Hồ Chí Minh", "Gọi trước khi giao", "COD");
        assertTrue(form.validate().isEmpty());
        assertEquals("+84912345678", form.getPhone());
        assertFalse(form.getAddress().contains("\n"));
    }
    @Test void returnsErrorsForEveryInvalidCheckoutField() {
        CheckoutDetails_24133057 form = new CheckoutDetails_24133057("<script>", "0123456789", "123", "x".repeat(501), "PAID");
        assertEquals(5, form.validate().size());
        assertEquals(4, new CheckoutDetails_24133057(null,null,null,null,null).validate().size());
        assertTrue(new CheckoutDetails_24133057("--", "0912345678", "!!!!!!!!!!!", "", "COD").validate().containsKey("recipient"));
        assertTrue(new CheckoutDetails_24133057("Nguyễn An", "0912345678", "!!!!!!!!!!!", "", "COD").validate().containsKey("address"));
    }
    @ParameterizedTest @ValueSource(strings={"1234567890", "0123456789", "091234567", "09123456789", "+840912345678", "abc", "0912345678<script>"})
    void rejectsInvalidVietnameseMobileNumbers(String phone) {
        assertTrue(new CheckoutDetails_24133057("Nguyễn An",phone,"12 Nguyễn Văn Bảo, TP Hồ Chí Minh","","COD").validate().containsKey("phone"));
    }
    @Test void limitsEachCartLineByBothStockAndQuantityCap() {
        Product_24133057 product = product(1, 3, true, "199000");
        assertTrue(new CartLine_24133057(product,3).isAvailable());
        assertFalse(new CartLine_24133057(product,4).isAvailable());
        assertFalse(new CartLine_24133057(product,0).isAvailable());
        assertFalse(new CartLine_24133057(product(1,500,true,"199000"),100).isAvailable());
        assertFalse(new CartLine_24133057(product(1,5,false,"199000"),1).isAvailable());
        assertFalse(new Cart_24133057(List.of()).isCheckoutAllowed());
    }
    @Test void totalsUseDecimalAndFingerprintChangesWithPriceOrQuantity() {
        Cart_24133057 cart = new Cart_24133057(List.of(new CartLine_24133057(product(1,10,true,"199000"),2),
                new CartLine_24133057(product(2,10,true,"149000"),3)));
        assertEquals(new BigDecimal("845000"), cart.getTotal());
        assertEquals(5, cart.getQuantity()); assertTrue(cart.isCheckoutAllowed());
        Cart_24133057 reversed = new Cart_24133057(List.of(cart.getLines().get(1), cart.getLines().get(0)));
        assertEquals(cart.fingerprint(), reversed.fingerprint());
        assertNotEquals(cart.fingerprint(), new Cart_24133057(List.of(new CartLine_24133057(product(1,10,true,"199001"),2),cart.getLines().get(1))).fingerprint());
        assertNotEquals(cart.fingerprint(), new Cart_24133057(List.of(new CartLine_24133057(product(1,10,true,"199000"),1),cart.getLines().get(1))).fingerprint());
    }
    @Test void providesExactlyEightFiltersAndOnlyEarlyCancellation() {
        assertEquals(8, OrderStatus_24133057.values().length);
        assertNull(OrderStatus_24133057.filter(""));
        assertEquals(OrderStatus_24133057.DELIVERED, OrderStatus_24133057.filter("DELIVERED"));
        assertThrows(IllegalArgumentException.class, () -> OrderStatus_24133057.filter("' OR 1=1 --"));
        for (OrderStatus_24133057 status : OrderStatus_24133057.values())
            assertEquals(status == OrderStatus_24133057.NEW || status == OrderStatus_24133057.CONFIRMED, status.isCancellable());
    }
    private Product_24133057 product(int id,int stock,boolean active,String price) {
        return new Product_24133057(id,"VID"+id,"Video học tập","","",new BigDecimal(price),stock,active);
    }
}
