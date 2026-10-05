package huuthang.filter;

import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.ConfigurableSiteMeshFilter;

/** Keep the shared layout when validation returns a 4xx or a service returns a 5xx response. */
public class LayoutFilter_24133057 extends ConfigurableSiteMeshFilter {
    @Override
    protected void applyCustomConfiguration(SiteMeshFilterBuilder builder) {
        builder.setIncludeErrorPages(true);
    }
}
