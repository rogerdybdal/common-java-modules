package no.nav.sbl.dialogarena.common.abac;

import mockit.Expectations;
import mockit.Tested;
import no.nav.sbl.dialogarena.common.abac.pep.MockXacmlRequest;
import no.nav.sbl.dialogarena.common.abac.pep.PdpService;
import no.nav.sbl.dialogarena.common.abac.pep.domain.response.*;
import org.apache.http.client.methods.HttpPost;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.setProperty;
import static no.nav.sbl.dialogarena.common.abac.TestUtils.getContentFromJsonFile;
import static no.nav.sbl.dialogarena.common.abac.TestUtils.prepareResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

public class PdpServiceTest {

    @Tested
    PdpService pdpService;

    @BeforeClass
    public static void setup() {
        setProperty("ldap.url", "www.something.com");
        setProperty("ldap.username", "username");
        setProperty("ldap.password", "supersecrectpassword");
    }

    @Test
    public void returnsResponse() throws IOException {

        new Expectations(PdpService.class) {{
            pdpService.doPost(withAny(new HttpPost()));
            result = prepareResponse(200, getContentFromJsonFile("xacmlresponse.json"));
        }};

        final XacmlResponse actualXacmlResponse = pdpService.askForPermission(MockXacmlRequest.getXacmlRequest());


        final XacmlResponse expectedXacmlResponse = getExpectedXacmlResponse();

        assertThat(actualXacmlResponse, is(equalTo(expectedXacmlResponse)));

    }

    private XacmlResponse getExpectedXacmlResponse() {
        final Advice advice = new Advice("no.nav.abac.advices.deny.reason",
                new AttributeAssignment("no.nav.abac.advice.fritekst", "Ikke tilgang"));

        final List<Advice> associatedAdvice = new ArrayList<>();
        associatedAdvice.add(advice);

        final Response response = new Response()
                .withDecision(Decision.Deny)
                .withAssociatedAdvice(associatedAdvice);
        final List<Response> responses = new ArrayList<>();
        responses.add(response);

        return new XacmlResponse()
                .withResponse(responses);
    }


}

