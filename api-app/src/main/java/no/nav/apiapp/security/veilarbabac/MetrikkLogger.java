package no.nav.apiapp.security.veilarbabac;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.common.auth.SubjectHandler;
import no.nav.metrics.MetricsFactory;
import org.slf4j.Logger;

import java.util.function.Supplier;

class MetrikkLogger {

    private final MeterRegistry meterRegistry = MetricsFactory.getMeterRegistry();

    private boolean erAvvik = false;
    private String action = "read";
    private Supplier<String> idSupplier = ()->"";
    private Logger logger;
    private String avvikbeskrivelse = "";

    MetrikkLogger(Logger logger, String action, Supplier<String> idSupplier) {
        this.action = action;
        this.idSupplier = idSupplier;
        this.logger = logger;
    }

    public MetrikkLogger logger(Logger logger) {
        this.logger = logger;
        return this;
    }

    void erAvvik(String beskrivelse) {
        this.erAvvik = true;
        this.avvikbeskrivelse = beskrivelse;
    }

    void loggMetrikk(Tilgangstype tilgangstype, boolean foretrekkVeilarbabac) {
        meterRegistry.counter("veilarbabac-pepclient",
                "tilgangstype",
                tilgangstype.name(),
                "identType",
                SubjectHandler.getIdentType().map(Enum::name).orElse("unknown"),
                "action",
                action,
                "avvik",
                Boolean.toString(erAvvik),
                "avviksbeskrivelse",
                avvikbeskrivelse,
                "foretrekkVeilarbAbac",
                Boolean.toString(foretrekkVeilarbabac)
        ).increment();

        if(erAvvik) {
            //
            logger.warn("Fikk avvik i tilgang for {}", idSupplier.get());
        }
    }

    enum Tilgangstype {
        PersonAktoerId,
        PersonFoedselsnummer,
        Enhet
    }
}