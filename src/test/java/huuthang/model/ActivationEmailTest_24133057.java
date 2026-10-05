package huuthang.model;

import huuthang.dao.*;
import huuthang.service.*;
import huuthang.service.impl.EmailServiceImpl_24133057;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ActivationEmailTest_24133057 {
    private final ActivationCode_24133057 code=new ActivationCode_24133057("ABCDEF0123456789ABCDEF0123456789", "Java <script>danger</script>", "VID1", "AVAILABLE", 1);
    @Test void malformedCodesAreRejectedAndFormattingIsNormalized() {
        assertThrows(IllegalArgumentException.class,()->ActivationDao_24133057.normalize(null));
        assertThrows(IllegalArgumentException.class,()->ActivationDao_24133057.normalize("Z".repeat(32)));
        assertEquals(code.getCode(),ActivationDao_24133057.normalize("abcdef01-2345-6789-abcd-ef0123456789"));
    }
    @Test void paidVideoEmailEscapesCustomerAndVideoContent() {
        String html=new EmailServiceImpl_24133057().activationHtml("An <img>",1,List.of(code),"http://localhost:8080/ROOT/account/activate");
        assertFalse(html.contains("<script>")); assertFalse(html.contains("<img>")); assertTrue(html.contains("&lt;script&gt;")); assertTrue(html.contains(code.getCode()));
    }
    @Test void unconfiguredMailDoesNotClaimQueueAndFailureRemainsRetryable() throws Exception {
        var mail=new FakeMail(); var queue=new FakeQueue(); var codes=new FakeCodes();
        var worker=new ActivationEmailSender_24133057(mail,queue,codes);
        assertFalse(worker.sendNext("http://localhost/activate")); assertEquals(0,queue.claims);
        mail.configured=true; mail.success=false; assertTrue(worker.sendNext("http://localhost/activate"));
        assertEquals(1,mail.sends); assertEquals(Boolean.FALSE,queue.result);
        mail.success=true; assertTrue(worker.sendNext("http://localhost/activate")); assertEquals(Boolean.TRUE,queue.result);
    }
    @Test void revokedCodesAreNeverSent() throws Exception {
        var mail=new FakeMail();mail.configured=true; var queue=new FakeQueue();var codes=new FakeCodes();codes.revoked=true;
        assertTrue(new ActivationEmailSender_24133057(mail,queue,codes).sendNext("http://localhost/activate"));
        assertEquals(0,mail.sends);assertEquals(Boolean.FALSE,queue.result);
    }
    private class FakeCodes extends ActivationDao_24133057 {boolean revoked;
        @Override public List<ActivationCode_24133057> orderCodes(String user,long id){return revoked?List.of(new ActivationCode_24133057(code.getCode(),code.getTitle(),code.getVideoId(),"REVOKED",1)):List.of(code);}
    }
    private static class FakeQueue extends ActivationEmailDao_24133057 {int claims;Boolean result;
        @Override public Optional<Delivery> claim(){claims++;return Optional.of(new Delivery(1,1,"test","Test","test@example.invalid"));}
        @Override public void finish(long id,boolean sent){result=sent;}
    }
    private static class FakeMail implements EmailService_24133057 {boolean configured,success;int sends;
        public boolean isConfigured(){return configured;}
        public boolean sendRegistrationOtp(String a,String b,String c){throw new AssertionError("No OTP expected");}
        public boolean sendVideoActivationCodes(String recipient,String name,long order,List<ActivationCode_24133057> codes,String url){sends++;return success;}
    }
}
