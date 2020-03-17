package no.nav.common.aktorregisterklient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;

public class AktorregisterHttpKlient implements AktorregisterKlient {

    private final static Logger LOGGER = LoggerFactory.getLogger(AktorregisterHttpKlient.class);

    private final String aktorregisterUrl;

    private final String consumingApplication;

    private final Supplier<String> tokenSupplier;

    private final ObjectMapper objectMapper;

    private final OkHttpClient client;

    public AktorregisterHttpKlient(String aktorregisterUrl, String consumingApplication, Supplier<String> tokenSupplier) {
        this.aktorregisterUrl = aktorregisterUrl;
        this.consumingApplication = consumingApplication;
        this.tokenSupplier = tokenSupplier;

        this.objectMapper = new ObjectMapper();
        this.client = new OkHttpClient();
    }

    @Override
    public Optional<String> hentFnr(String aktorId) {
        return hentEnkeltIdent(aktorId, Identgruppe.NorskIdent);
    }

    @Override
    public Optional<String> hentAktorId(String fnr) {
        return hentEnkeltIdent(fnr, Identgruppe.AktoerId);
    }

    @Override
    public List<IdentOppslag> hentFnr(List<String> aktorIdListe) {
       return hentFlereIdenter(aktorIdListe, Identgruppe.NorskIdent);
    }

    @Override
    public List<IdentOppslag> hentAktorId(List<String> fnrListe) {
        return hentFlereIdenter(fnrListe, Identgruppe.AktoerId);
    }

    private Optional<String> hentEnkeltIdent(String aktorIdEllerFnr, Identgruppe identgruppe) {
        return hentIdenter(Collections.singletonList(aktorIdEllerFnr), identgruppe)
                .entrySet()
                .stream()
                .filter(this::filtrerIkkeGjeldendeIdent)
                .findFirst()
                .flatMap(e -> finnGjeldendeIdent(e.getValue().identer))
                .map(i -> i.ident);
    }

    private List<IdentOppslag> hentFlereIdenter(List<String> aktorIdEllerFnrListe, Identgruppe identgruppe) {
        return hentIdenter(aktorIdEllerFnrListe, identgruppe)
                .entrySet()
                .stream()
                .map(this::tilIdentOppslag)
                .collect(Collectors.toList());
    }

    private String createRequestUrl(String aktorregisterUrl, Identgruppe identgruppe) {
        return String.format("%s/identer?gjeldende=true&identgruppe=%s", aktorregisterUrl, valueOf(identgruppe));
    }

    private boolean filtrerIkkeGjeldendeIdent(Map.Entry<String, IdentData> identEntry) {
        List<Ident> identer = identEntry.getValue().identer;
        return identer != null && finnGjeldendeIdent(identer).isPresent();
    }

    private Optional<Ident> finnGjeldendeIdent(List<Ident> identer) {
        return identer.stream().filter(ident -> ident.gjeldende).findFirst();
    }

    private IdentOppslag tilIdentOppslag(Map.Entry<String, IdentData> identEntry) {
        Optional<Ident> gjeldendeIdent = finnGjeldendeIdent(identEntry.getValue().identer);
        return new IdentOppslag(identEntry.getKey(), gjeldendeIdent.map(i -> i.ident).orElse(null));
    }

    private Map<String, IdentData> hentIdenter(List<String> fnrEllerAtkorIder, Identgruppe identgruppe) {
        String personidenter = String.join(",", fnrEllerAtkorIder);

        Request request = new Request.Builder()
                .url(createRequestUrl(aktorregisterUrl, identgruppe))
                .addHeader("Nav-Call-Id", UUID.randomUUID().toString())
                .addHeader("Nav-Consumer-Id", consumingApplication)
                .addHeader("Nav-Personidenter", personidenter)
                .addHeader("Authorization", "Bearer " + tokenSupplier.get())
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                return Collections.emptyMap();
            }

            String body = response.body().string();
            return objectMapper.readValue(body, new TypeReference<Map<String, IdentData>>() {});
        } catch (Exception e) {
            LOGGER.error("Klarte ikke å gjore oppslag mot aktorregister", e);
            return Collections.emptyMap();
        }
    }

    enum Identgruppe {
        NorskIdent, AktoerId
    }

    private static class IdentData {

        public List<Ident> identer;

        public String feilmelding;

    }

    private static class Ident {

        public String ident; // fnr eller aktorid

        public Identgruppe identgruppe;

        public boolean gjeldende;

    }

}