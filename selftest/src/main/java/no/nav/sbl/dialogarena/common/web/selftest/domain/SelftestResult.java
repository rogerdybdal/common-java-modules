package no.nav.sbl.dialogarena.common.web.selftest.domain;

import lombok.Builder;
import lombok.Value;
import no.nav.sbl.dialogarena.common.web.selftest.SelfTestStatus;

import static java.util.Optional.ofNullable;

@Value
@Builder
public class SelftestResult {

    private String id;
    private String endpoint;
    private String description;
    private String errorMessage;
    private SelfTestStatus result;
    private long responseTime;
    private String stacktrace;
    private boolean critical;


    public SelfTestStatus getResult() {
        return ofNullable(result).orElse(SelfTestStatus.ERROR);
    }

    public boolean harFeil() {
        SelfTestStatus result = getResult();
        return result != SelfTestStatus.OK && result != SelfTestStatus.DISABLED;
    }

}