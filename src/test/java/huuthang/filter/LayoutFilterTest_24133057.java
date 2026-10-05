package huuthang.filter;

import org.junit.jupiter.api.Test;
import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.webapp.contentfilter.Selector;
import static org.junit.jupiter.api.Assertions.*;

class LayoutFilterTest_24133057 {
    private Selector selector() {
        SiteMeshFilterBuilder builder = new SiteMeshFilterBuilder();
        new LayoutFilter_24133057().applyCustomConfiguration(builder);
        return builder.getSelector();
    }
    @Test void validationAndErrorPagesKeepTheSharedLayout() {
        for (int status : new int[]{200,400,401,403,404,409,422,500,503})
            assertFalse(selector().shouldAbortBufferingForHttpStatusCode(status), "Missing layout for HTTP " + status);
    }
    @Test void redirectsAndStaticAssetsAreNotDecorated() {
        for (int status : new int[]{204,301,302,303,304,307,308})
            assertTrue(selector().shouldAbortBufferingForHttpStatusCode(status));
        assertTrue(selector().shouldBufferForContentType("text/html;charset=UTF-8", "text/html", "UTF-8"));
        assertFalse(selector().shouldBufferForContentType("text/css", "text/css", "UTF-8"));
        assertFalse(selector().shouldBufferForContentType("application/javascript", "application/javascript", "UTF-8"));
    }
}
