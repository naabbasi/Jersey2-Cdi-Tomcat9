package edu.learn.cdi.demo.filter;

import edu.learn.cdi.demo.listener.metrics.WebMetricsListener;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(displayName = "CORS_Filter", urlPatterns = "*")
public class CORSFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        response.setHeader("Access-Control-Allow-Origin", "http://noman");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

        // Mark request as processed (for DropWizard REST metrics)
        WebMetricsListener.METRIC_REGISTRY.meter("requests").mark();

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {

        }
    }

    @Override
    public void destroy() {

    }
}
