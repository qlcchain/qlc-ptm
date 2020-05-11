package com.quorum.tessera.server.jaxrs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class LoggingFilterTest {

    private LoggingFilter loggingFilter;

    public LoggingFilterTest() {}

    @Before
    public void setUp() {
        loggingFilter = new LoggingFilter();
    }

    @After
    public void tearDown() {
        loggingFilter = null;
    }

    @Test
    public void filterRequest() {
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        loggingFilter.filter(request);
        // Very silly test
        assertThat(loggingFilter).isNotNull();
    }

    @Test
    public void filterRequestAndResponse() {
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        ContainerResponseContext response = mock(ContainerResponseContext.class);
        Response.StatusType statusInfo = mock(Response.StatusType.class);
        when(response.getStatusInfo()).thenReturn(statusInfo);
        loggingFilter.filter(request, response);

        // Very silly test
        assertThat(loggingFilter).isNotNull();
    }

    @Test
    public void filterNullResourceInfo() {
        ContainerRequestContext request = mock(ContainerRequestContext.class);
        loggingFilter.setResourceInfo(null);
        loggingFilter.filter(request); // if this doesn't throw exception then test passed
    }

    @Test
    public void filterNullResourceInfoClass() {
        ResourceInfo resourceInfo = mock(ResourceInfo.class);
        loggingFilter.setResourceInfo(resourceInfo);
        when(resourceInfo.getResourceClass()).thenReturn(null);

        ContainerRequestContext request = mock(ContainerRequestContext.class);
        loggingFilter.filter(request); // if this doesn't throw exception then test passed
    }
}
