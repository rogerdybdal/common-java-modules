package no.nav.sbl.dialogarena.common.abac;

import com.fasterxml.jackson.databind.node.ObjectNode;
import no.nav.abac.xacml.NavAttributter;
import no.nav.json.JsonUtils;
import no.nav.sbl.dialogarena.common.abac.pep.MockXacmlRequest;
import no.nav.sbl.dialogarena.common.abac.pep.XacmlMapper;
import no.nav.sbl.dialogarena.common.abac.pep.domain.response.*;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static no.nav.sbl.dialogarena.common.abac.TestUtils.getContentFromJsonFile;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;


public class XacmlMapperTest {

    private static final String ID1 = "no.nav.abac.advice.ingen_tilgang";
    private static final String ID2 = "no.nav.abac.advices.deny.reason";

    @Test
    public void convertsRequestToJson() throws IOException {
        final String stringEntity = XacmlMapper.mapRequestToEntity(MockXacmlRequest.getXacmlRequest());

        String expectedContent = getContentFromJsonFile("xacmlrequest-withtoken.json");
        assertJson(stringEntity, expectedContent);
    }

    @Test
    public void convertRequestWithSubjectAttributesToJson() throws IOException {
        final String stringEntity = XacmlMapper.mapRequestToEntity(MockXacmlRequest.getXacmlRequestWithSubjectAttributes());

        String expectedContent = getContentFromJsonFile("xacmlrequest-withsubjectattributes.json");
        assertJson(stringEntity, expectedContent);
    }

    @Test
    public void convertRequestWithSubjAttrWithoutEnvironmentToJson() throws IOException {
        final String stringEntity = XacmlMapper.mapRequestToEntity(MockXacmlRequest.getXacmlRequestWithSubjAttrWithoutEnvironment());

        String expectedContent = getContentFromJsonFile("xacmlrequest-withsubjattrwithoutenvironment.json");
        assertJson(stringEntity, expectedContent);
    }

    @Test
    public void convertRequestWithSubjectAndKode6() throws IOException {
        final String stringEntity = XacmlMapper.mapRequestToEntity(MockXacmlRequest.getXacmlRequestForSubjectWithKode6Resource());

        String expectedContent = getContentFromJsonFile("xacmlrequest-kode6.json");
        assertJson(stringEntity, expectedContent);

    }

    @Test
    public void convertRequestWithSubjectAndKode7() throws IOException {
        final String stringEntity = XacmlMapper.mapRequestToEntity(MockXacmlRequest.getXacmlRequestForSubjectWithKode7Resource());

        String expectedContent = getContentFromJsonFile("xacmlrequest-kode7.json");
        assertJson(stringEntity, expectedContent);
    }

    @Test
    public void convertRequestWithSubjectAndVeilarb() throws IOException {
        final String stringEntity = XacmlMapper.mapRequestToEntity(MockXacmlRequest.getXacmlRequestForSubjectWithVeilArbResource());

        String expectedContent = getContentFromJsonFile("xacmlrequest-veilarb.json");
        assertJson(stringEntity, expectedContent);
    }

    @Test
    public void convertRequestWithSubjectAndEgenAnsatt() throws IOException {
        final String stringEntity = XacmlMapper.mapRequestToEntity(MockXacmlRequest.getXacmlRequestForSubjectWithEgenAnsattResource());

        String expectedContent = getContentFromJsonFile("xacmlrequest-egenAnsatt.json");
        assertJson(stringEntity,expectedContent);
    }

    @Test
    public void convertsSimpleJsonToResponse() throws IOException {
        final XacmlResponse actualResponse = XacmlMapper
                .mapRawResponse(getContentFromJsonFile("xacmlresponse-simple.json"));

        XacmlResponse expectedResponse = getXacmlResponse();

        assertThat(actualResponse, is(equalTo(expectedResponse)));
    }

    @Test
    public void convertsSimpleJsonWithArrayToResponse() throws IOException {
        final XacmlResponse actualResponse = XacmlMapper
                .mapRawResponse(getContentFromJsonFile("xacmlresponse-simple-with-array.json"));
        XacmlResponse expectedResponse = getXacmlResponse();

        assertThat(actualResponse, is(equalTo(expectedResponse)));
    }

    @Test
    public void convertsJsonWithAdvicesToResponse() throws IOException {
        final XacmlResponse actualResponse = XacmlMapper
                .mapRawResponse(getContentFromJsonFile("xacmlresponse-multiple-advice.json"));
        XacmlResponse expectedResponse = getXacmlResponseWithAdvices();

        assertThat(actualResponse, is(equalTo(expectedResponse)));
    }

    @Test
    public void convertsJsonWithAdvices2ToResponse() throws IOException {
        final XacmlResponse actualResponse = XacmlMapper
                .mapRawResponse(getContentFromJsonFile("xacmlresponse-with-attributeassignmentlist.json"));
        XacmlResponse expectedResponse = getXacmlResponseWithAdvices2();

        assertThat(actualResponse, is(equalTo(expectedResponse)));
    }

    private XacmlResponse getXacmlResponse() {
        List<Response> responses = new ArrayList<>();
        responses.add(new Response().withDecision(Decision.Permit));
        return new XacmlResponse().withResponse(responses);
    }

    private XacmlResponse getXacmlResponseWithAdvices() {
        List<Advice> associatedAdvice = new ArrayList<>();

        final AttributeAssignment attributeAssignment1 = new AttributeAssignment("no.nav.abac.advice.fritekst", "Mangler konsument (consumerId)");

        final List<AttributeAssignment> attributeAssignments1 = new ArrayList<>();
        attributeAssignments1.add(attributeAssignment1);

        final Advice advice1 = new Advice(ID1, attributeAssignments1);
        associatedAdvice.add(advice1);


        final AttributeAssignment attributeAssignment2 = new AttributeAssignment("no.nav.abac.advice.fritekst", "Mangler autentiseringsNivaa (authenticationLevel)");
        final List<AttributeAssignment> attributeAssignments2 = new ArrayList<>();
        attributeAssignments2.add(attributeAssignment2);

        final Advice advice2 = new Advice(ID2, attributeAssignments2);
        associatedAdvice.add(advice2);

        List<Response> responses = new ArrayList<>();
        responses.add(new Response().withDecision(Decision.Deny).withAssociatedAdvice(associatedAdvice));

        return new XacmlResponse().withResponse(responses);
    }

    private XacmlResponse getXacmlResponseWithAdvices2() {
        List<Advice> associatedAdvice = new ArrayList<>();

        final AttributeAssignment attributeAssignment1 = new AttributeAssignment(NavAttributter.ADVICEOROBLIGATION_CAUSE, "cause-0001-manglerrolle");
        final AttributeAssignment attributeAssignment2 = new AttributeAssignment(NavAttributter.ADVICEOROBLIGATION_DENY_POLICY, "veilarb_pilot_tilgang");
        final AttributeAssignment attributeAssignment3 = new AttributeAssignment(NavAttributter.ADVICEOROBLIGATION_DENY_RULE, "veilarb_pilot_tilgang_deny_all");

        final List<AttributeAssignment> attributeAssignments = new ArrayList<>();
        attributeAssignments.add(attributeAssignment1);
        attributeAssignments.add(attributeAssignment2);
        attributeAssignments.add(attributeAssignment3);

        final Advice advice2 = new Advice("no.nav.abac.advices.reason.deny_reason", attributeAssignments);
        associatedAdvice.add(advice2);

        List<Response> responses = new ArrayList<>();
        responses.add(new Response().withDecision(Decision.Deny).withAssociatedAdvice(associatedAdvice));

        return new XacmlResponse().withResponse(responses);
    }

    private void assertJson(String json, String expectedJson) {
        ObjectNode objectNode = JsonUtils.fromJson(json, ObjectNode.class);
        ObjectNode expectedObjectNode = JsonUtils.fromJson(expectedJson, ObjectNode.class);
        assertThat(objectNode, equalTo(expectedObjectNode));
    }

}