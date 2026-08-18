package com.mirkoebert.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class RequestTracingFilterTest {

    private final RequestTracingFilter filter = new RequestTracingFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void acceptsAlphanumericUnderscoreHyphenUpToMaxLength() throws Exception {
        String incoming = "abcDEF_012-xyz";
        MockHttpServletResponse response = filterRequest(incoming);

        assertThat(response.getHeader(RequestTracingFilter.TRACE_ID_HEADER)).isEqualTo(incoming);
        assertThat(RequestTracingFilter.isValidTraceId(incoming)).isTrue();
        assertThat(RequestTracingFilter.isValidTraceId("a".repeat(RequestTracingFilter.MAX_TRACE_ID_LENGTH))).isTrue();
    }

    @Test
    void rejectsMissingBlankTooLongAndIllegalCharacters() throws Exception {
        assertThat(RequestTracingFilter.isValidTraceId(null)).isFalse();
        assertThat(RequestTracingFilter.isValidTraceId("")).isFalse();
        assertThat(RequestTracingFilter.isValidTraceId("   ")).isFalse();
        assertThat(RequestTracingFilter.isValidTraceId("a".repeat(RequestTracingFilter.MAX_TRACE_ID_LENGTH + 1))).isFalse();
        assertThat(RequestTracingFilter.isValidTraceId("abc def")).isFalse();
        assertThat(RequestTracingFilter.isValidTraceId("id\r\nSet-Cookie:x")).isFalse();
        assertThat(RequestTracingFilter.isValidTraceId("id;drop")).isFalse();
        assertThat(RequestTracingFilter.isValidTraceId("id/../x")).isFalse();

        MockHttpServletResponse missing = filterRequest(null);
        assertGeneratedTraceId(missing.getHeader(RequestTracingFilter.TRACE_ID_HEADER));

        MockHttpServletResponse injected = filterRequest("foo\r\nX-Injected: yes");
        assertGeneratedTraceId(injected.getHeader(RequestTracingFilter.TRACE_ID_HEADER));
        assertThat(injected.getHeader("X-Injected")).isNull();

        MockHttpServletResponse tooLong = filterRequest("a".repeat(RequestTracingFilter.MAX_TRACE_ID_LENGTH + 1));
        assertGeneratedTraceId(tooLong.getHeader(RequestTracingFilter.TRACE_ID_HEADER));
    }

    @Test
    void clearsMdcAfterRequest() throws Exception {
        filterRequest("trace-ok");
        assertThat(MDC.get(RequestTracingFilter.TRACE_ID_MDC_KEY)).isNull();
    }

    private MockHttpServletResponse filterRequest(String headerValue) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (headerValue != null) {
            request.addHeader(RequestTracingFilter.TRACE_ID_HEADER, headerValue);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private static void assertGeneratedTraceId(String traceId) {
        assertThat(traceId).isNotBlank();
        assertThat(RequestTracingFilter.isValidTraceId(traceId)).isTrue();
        assertThat(traceId).hasSize(8);
    }
}
