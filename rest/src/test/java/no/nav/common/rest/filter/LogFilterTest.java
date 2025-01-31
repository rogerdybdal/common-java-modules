package no.nav.common.rest.filter;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.HttpHeaders.SET_COOKIE;
import static no.nav.common.rest.filter.LogRequestFilter.NAV_CALL_ID_HEADER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

public class LogFilterTest {

    private static final Logger LOG = getLogger(LogFilterTest.class);

    private MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
    private HttpServletResponse httpServletResponse = new MockHttpServletResponse();

    private LogRequestFilter logRequestFilter = new LogRequestFilter("test");

    @Before
    public void setup() {
        httpServletRequest.setMethod("GET");
        httpServletRequest.setRequestURI("/test/path");
    }

    @Test
    public void smoketest() throws ServletException, IOException {
        logRequestFilter.doFilter(httpServletRequest, httpServletResponse, (request, response) -> LOG.info("testing logging 1"));
        logRequestFilter.doFilter(httpServletRequest, httpServletResponse, (request, response) -> LOG.info("testing logging 2"));
        logRequestFilter.doFilter(httpServletRequest, httpServletResponse, (request, response) -> LOG.info("testing logging 3"));
    }

    @Test
    public void cleanupOfMDCContext() throws ServletException, IOException {
        Map<String, String> initialContextMap = Optional.ofNullable(MDC.getCopyOfContextMap()).orElseGet(HashMap::new);
        logRequestFilter.doFilter(httpServletRequest, httpServletResponse, (request, response) -> {});
        assertThat(initialContextMap).isEqualTo(MDC.getCopyOfContextMap());
    }

    @Test
    public void addResponseHeaders() throws ServletException, IOException {
        logRequestFilter.doFilter(httpServletRequest, httpServletResponse, (request, response) -> {});

        assertThat(httpServletResponse.getHeader(NAV_CALL_ID_HEADER_NAME)).isNotEmpty();
        assertThat(httpServletResponse.getHeader(SET_COOKIE)).isNotEmpty();
    }

    @Test
    public void handleExceptions() throws ServletException, IOException {
        logRequestFilter.doFilter(httpServletRequest, httpServletResponse, (request, response) -> fail());
        assertThat(httpServletResponse.getStatus()).isEqualTo(SC_INTERNAL_SERVER_ERROR);
    }

    private void fail() {
        throw new IllegalStateException();
    }

}