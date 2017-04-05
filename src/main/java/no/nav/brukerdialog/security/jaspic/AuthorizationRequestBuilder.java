package no.nav.brukerdialog.security.jaspic;

import no.nav.brukerdialog.tools.HostUtils;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.SecureRandom;

import static java.lang.System.*;

public class AuthorizationRequestBuilder {

    private static final SecureRandom random = new SecureRandom();

    private final String server;
    private final String scope = "openid";
    private boolean useKerberos = true;
    private String stateIndex;
    private static final String DEFAULT_REDIRECT_URL = "/" + getProperty("applicationName") + "/tjenester/login";


    public AuthorizationRequestBuilder(HttpServletRequest req) {
        server = HostUtils.formatSchemeHostPort(req);
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        stateIndex = "state_" + new BigInteger(1, bytes).toString(16);
    }

    public AuthorizationRequestBuilder ikkeBrukKerberos() {
        useKerberos = false;
        return this;
    }

    public String getStateIndex() {
        return stateIndex;
    }

    public String buildRedirectString() throws UnsupportedEncodingException {
        String clientId = getProperty("isso-rp-user.username");
        String state = stateIndex;
        String redirectUrl = getProperty("oidc-redirect.url") == null ? server + DEFAULT_REDIRECT_URL : getProperty("oidc-redirect.url");
        String kerberosTrigger = useKerberos
                ? "session=winssochain&authIndexType=service&authIndexValue=winssochain&"
                : "";
        return String.format("%s/authorize?" + kerberosTrigger + "response_type=code&scope=%s&client_id=%s&state=%s&redirect_uri=%s",
                getProperty("isso-host.url"),
                scope,
                URLEncoder.encode(clientId, "UTF-8"),
                state,
                URLEncoder.encode(redirectUrl, "UTF-8")
        );
    }
}