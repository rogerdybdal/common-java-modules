package no.nav.sbl.dialogarena.common.abac.pep.service;

import no.nav.sbl.dialogarena.common.abac.pep.MockXacmlRequest;
import no.nav.sbl.dialogarena.common.abac.pep.domain.Attribute;
import no.nav.sbl.dialogarena.common.abac.pep.domain.response.*;
import no.nav.sbl.dialogarena.common.abac.pep.exception.AbacException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response.Status.Family;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static javax.ws.rs.core.Response.Status.Family.familyOf;
import static no.nav.sbl.dialogarena.common.abac.TestUtils.getContentFromJsonFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbacServiceTest {

    private Client client = mock(Client.class);
    private WebTarget webTarget = mock(WebTarget.class);
    private Builder requestBuilder = mock(Builder.class);
    private javax.ws.rs.core.Response response = mock(javax.ws.rs.core.Response.class);

    private AbacService abacService = new AbacService(client, AbacServiceConfig.builder()
            .endpointUrl("/test/endpoint")
            .build());

    @Before
    public void setup() {
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(requestBuilder);
        when(requestBuilder.post(any())).thenReturn(response);
    }

    @Test
    public void returnsResponse() throws IOException, AbacException, NoSuchFieldException {
        gitt_response(200);
        gitt_responseEntity(getContentFromJsonFile("xacmlresponse.json"));

        final XacmlResponse actualXacmlResponse = abacService.askForPermission(MockXacmlRequest.getXacmlRequest());

        final XacmlResponse expectedXacmlResponse = getExpectedXacmlResponse();

        assertThat(actualXacmlResponse, is(equalTo(expectedXacmlResponse)));
    }

    @Test
    public void returnsResponseWithMultipleDecisions() throws IOException, AbacException, NoSuchFieldException {
        gitt_response(200);
        gitt_responseEntity(getContentFromJsonFile("xacmlresponse-multiple-decision-and-category.json"));

        final XacmlResponse actualXacmlResponse = abacService.askForPermission(MockXacmlRequest.getXacmlRequest());

        final XacmlResponse expectedXacmlResponse = new XacmlResponse();
        List<Response> responses = Arrays.asList(
                new Response()
                    .withDecision(Decision.Permit)
                    .withCategory(
                            new Category(
                    "urn:oasis:names:tc:xacml:3.0:attribute-category:resource",
                                new Attribute("no.nav.abac.attributter.resource.felles.person.fnr", "11111111111")
                            )
                    ),

                new Response()
                        .withDecision(Decision.Deny)
                        .withCategory(
                                new Category(
                                        "urn:oasis:names:tc:xacml:3.0:attribute-category:resource",
                                        new Attribute("no.nav.abac.attributter.resource.felles.person.fnr", "22222222222")
                                )
                        )
        );

        expectedXacmlResponse.setResponse(responses);

        assertThat(actualXacmlResponse, is(equalTo(expectedXacmlResponse)));
    }

    @Test(expected = AbacException.class)
    public void throwsExceptionAt500Error() throws IOException, AbacException, NoSuchFieldException {
        gitt_response(SC_INTERNAL_SERVER_ERROR);
        abacService.askForPermission(MockXacmlRequest.getXacmlRequest());
    }

    @Test(expected = ClientErrorException.class)
    public void throwsExceptionAt400Error() throws IOException, AbacException, NoSuchFieldException {
        gitt_response(SC_UNAUTHORIZED);
        abacService.askForPermission(MockXacmlRequest.getXacmlRequest());
    }

    private XacmlResponse getExpectedXacmlResponse() {

        final List<AttributeAssignment> attributeAssignments = new ArrayList<>();
        attributeAssignments.add(new AttributeAssignment("no.nav.abac.advice.fritekst", "Ikke tilgang"));

        final Advice advice = new Advice("no.nav.abac.advices.reason.deny_reason", attributeAssignments);

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


    private void gitt_responseEntity(String contentFromJsonFile) {
        when(response.readEntity(String.class)).thenReturn(contentFromJsonFile);
    }

    private void gitt_response(int statusCode) {
        when(response.getStatus()).thenReturn(statusCode);
        when(response.getStatusInfo()).thenReturn(new javax.ws.rs.core.Response.StatusType() {
            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public Family getFamily() {
                return familyOf(statusCode);
            }

            @Override
            public String getReasonPhrase() {
                return Integer.toString(statusCode);
            }
        });
    }

}
