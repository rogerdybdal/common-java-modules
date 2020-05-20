package no.nav.common.utils;

import no.nav.common.test.junit.SystemPropertiesRule;
import org.junit.Test;

import static no.nav.common.utils.EnvironmentUtils.NAIS_NAMESPACE_PROPERTY_NAME;
import static no.nav.common.utils.UrlUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UrlUtilsTest {

    public SystemPropertiesRule systemPropertiesRule = new SystemPropertiesRule();

    @Test
    public void test_clusterUrlForApplication() {
        assertThatThrownBy(() -> clusterUrlForApplication("app1")).hasMessageContaining(NAIS_NAMESPACE_PROPERTY_NAME);

        systemPropertiesRule.setProperty(NAIS_NAMESPACE_PROPERTY_NAME, "q0");
        assertThat(clusterUrlForApplication("app1")).isEqualTo("http://app1.q0.svc.nais.local");
        assertThat(clusterUrlForApplication("app2")).isEqualTo("http://app2.q0.svc.nais.local");

        systemPropertiesRule.setProperty(NAIS_NAMESPACE_PROPERTY_NAME, "default");
        assertThat(clusterUrlForApplication("app1")).isEqualTo("http://app1.default.svc.nais.local");
        assertThat(clusterUrlForApplication("app2")).isEqualTo("http://app2.default.svc.nais.local");
    }

    @Test
    public void test_sluttMedSlash() {
        assertThat(sluttMedSlash(null)).isEqualTo("/");
        assertThat(sluttMedSlash("")).isEqualTo("/");
        assertThat(sluttMedSlash("/")).isEqualTo("/");
        assertThat(sluttMedSlash("/abc")).isEqualTo("/abc/");
    }

    @Test
    public void test_startMedSlash() {
        assertThat(startMedSlash(null)).isEqualTo("/");
        assertThat(startMedSlash("")).isEqualTo("/");
        assertThat(startMedSlash("abc")).isEqualTo("/abc");
        assertThat(startMedSlash("/abc")).isEqualTo("/abc");
    }


    @Test
    public void test_joinPaths() {
        assertThat(joinPaths(null)).isEqualTo("/");
        assertThat(joinPaths(null,null,null)).isEqualTo("/");
        assertThat(joinPaths("/","","/","/")).isEqualTo("/");
        assertThat(joinPaths("abc","def")).isEqualTo("/abc/def");
        assertThat(joinPaths("/abc","def")).isEqualTo("/abc/def");
        assertThat(joinPaths("abc/","def")).isEqualTo("/abc/def");
        assertThat(joinPaths("abc","/def")).isEqualTo("/abc/def");
        assertThat(joinPaths("abc","def/")).isEqualTo("/abc/def/");
        assertThat(joinPaths("/abc/","/def/")).isEqualTo("/abc/def/");
        assertThat(joinPaths("/abc/","","/def/")).isEqualTo("/abc/def/");
        assertThat(joinPaths("/abc/","/","/def/")).isEqualTo("/abc/def/");
        assertThat(joinPaths("http://","abc","def")).isEqualTo("http://abc/def");
        assertThat(joinPaths("http://abc","def","ghi")).isEqualTo("http://abc/def/ghi");
        assertThat(joinPaths("http://abc/","/def","ghi")).isEqualTo("http://abc/def/ghi");
        assertThat(joinPaths("http://abc/","def","ghi")).isEqualTo("http://abc/def/ghi");
        assertThat(joinPaths("http://abc/","def/","ghi")).isEqualTo("http://abc/def/ghi");
        assertThat(joinPaths("ftp://","/abc","def")).isEqualTo("ftp://abc/def");
    }

}