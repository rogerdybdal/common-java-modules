package no.nav.sbl.dialogarena.common.abac;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import no.nav.modig.core.context.SAMLAssertionCredential;
import no.nav.sbl.dialogarena.common.abac.pep.CredentialConstants;
import no.nav.sbl.dialogarena.common.abac.pep.Pep;
import no.nav.sbl.dialogarena.common.abac.pep.context.AbacContext;
import no.nav.sbl.dialogarena.common.abac.pep.domain.response.BiasedDecisionResponse;
import no.nav.sbl.dialogarena.common.abac.pep.domain.response.Decision;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.security.auth.Subject;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AbacContext.class})
public class HttpTest {

    @Inject
    Pep pep;

    private static MockWebServer server;
    private static final MockResponse MOCK_RESPONSE = new MockResponse()
            .throttleBody(16, 250, TimeUnit.MILLISECONDS)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .addHeader("Cache-Control", "no-cache")
            .setBody("{\"Response\":{\"Decision\":\"Permit\"}}");

    @BeforeClass
    public static void setup() throws IOException {
        System.setProperty("abac.bibliotek.simuler.avbrudd", "false");
        System.setProperty(no.nav.brukerdialog.security.context.SubjectHandler.SUBJECTHANDLER_KEY, no.nav.brukerdialog.security.context.ThreadLocalSubjectHandler.class.getName());
        System.setProperty(no.nav.modig.core.context.SubjectHandler.SUBJECTHANDLER_KEY, no.nav.modig.core.context.ThreadLocalSubjectHandler.class.getName());
        System.setProperty(CredentialConstants.SYSTEMUSER_USERNAME, "username");
        System.setProperty(CredentialConstants.SYSTEMUSER_PASSWORD, "password");
        System.setProperty("ldap.url", "ldap.url");
        System.setProperty("ldap.username", "ldap.username");
        System.setProperty("ldap.password", "ldap.password");
        System.setProperty("ldap.fallback", "false");
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);


        server = new MockWebServer();
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest recordedRequest) throws InterruptedException {
                return MOCK_RESPONSE;
            }
        });
        server.start();

        HttpUrl url = server.url("/asm-pdp/authorize");
        System.setProperty("abac.endpoint.url", url.toString());
    }

    @AfterClass
    public static void teardown() throws IOException {
        server.shutdown();
    }

    @Test
    public void doesItWork() throws Exception {
        BiasedDecisionResponse response = pep.isServiceCallAllowedWithIdent("id", "domain", "10108000398");
        assertThat(response.getBiasedDecision(), Matchers.equalTo(Decision.Permit));
    }

    @Test
    public void harInnloggetBrukerTilgangTilPerson() throws Exception {
        gittBrukerMedSAMLAssertion();
        BiasedDecisionResponse response = pep.harInnloggetBrukerTilgangTilPerson("10108000398", "domain");
        assertThat(response.getBiasedDecision(), Matchers.equalTo(Decision.Permit));
    }

    @Test
    public void performance() throws Exception {
        int TESTS = 1000;
        ForkJoinPool fjp = new ForkJoinPool(32);
        List<Callable<BiasedDecisionResponse>> tasks = IntStream.range(0, TESTS)
                .mapToObj((i) -> (Callable<BiasedDecisionResponse>) () -> pep.isServiceCallAllowedWithIdent("id" + String.valueOf(i), "domain", "10108000398"))
                .collect(Collectors.toList());

        List<Future<BiasedDecisionResponse>> futures = fjp.invokeAll(tasks);
        futures.forEach((result) -> {
            try {
                assertThat(result.isDone(), equalTo(true));
                assertThat(result.get().getBiasedDecision(), equalTo(Decision.Permit));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void gittBrukerMedSAMLAssertion() throws ParserConfigurationException {
        Subject subject = new Subject();
        subject.getPublicCredentials().add(new SAMLAssertionCredential(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getDocumentElement()));
        new no.nav.brukerdialog.security.context.ThreadLocalSubjectHandler().setSubject(subject);
    }

}
