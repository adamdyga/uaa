package org.cloudfoundry.identity.uaa.oauth;

import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenValidityResolverTest {

    private TokenValidityResolver resolver;
    private ClientTokenValidity clientTokenValidity;

    @Before
    public void setup() {
        int globalAccessTokenValiditySeconds = 120;

        DateTimeUtils.setCurrentMillisFixed(1000L);

        clientTokenValidity = mock(ClientTokenValidity.class);
        when(clientTokenValidity.getValiditySeconds("clientId")).thenReturn(100);

        resolver = new TokenValidityResolver(clientTokenValidity, globalAccessTokenValiditySeconds);
    }

    @After
    public void teardown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void whenClientValidityConfigured() {
        Date validity = resolver.resolve("clientId");

        assertThat(validity.getTime(), is(101_000l));
    }


    @Test
    public void whenClientValidityNotConfigured_fallsBackToZoneConfiguration() {
        when(clientTokenValidity.getZoneValiditySeconds()).thenReturn(50);
        when(clientTokenValidity.getValiditySeconds("clientId")).thenReturn(null);

        Date validity = resolver.resolve("clientId");

        assertThat(validity.getTime(), is(51_000l));
    }

    @Test
    public void whenZoneValidityNotConfigured_fallsBackToGlobalPolicy() {
        when(clientTokenValidity.getZoneValiditySeconds()).thenReturn(-1);
        when(clientTokenValidity.getValiditySeconds("clientId")).thenReturn(null);

        Date validity = resolver.resolve("clientId");

        assertThat(validity.getTime(), is(121_000l));
    }

}