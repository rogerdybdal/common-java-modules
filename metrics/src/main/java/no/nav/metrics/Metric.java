package no.nav.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static no.nav.modig.common.MDCOperations.*;

abstract class Metric<T extends Metric> {
    protected final MetricsClient metricsClient;
    protected final String name;
    protected Map<String, Object> fields = new HashMap<>();
    protected Map<String, String> tags = new HashMap<>();

    private static final String[] MDC_VARIABLES = {
            MDC_CALL_ID,
            MDC_CONSUMER_ID,
            MDC_USER_ID,
    };

    Metric(MetricsClient metricsClient, String name) {
        this.metricsClient = metricsClient;
        this.name = name;
        setSuccess();
        for (String mdcVariable : MDC_VARIABLES) {
            String mdcValue = getFromMDC(mdcVariable);
            if(Objects.nonNull(mdcValue)) {
                addFieldToReport(mdcVariable, mdcValue);
            }
        }
    }

    public T addFieldToReport(String fieldName, Object value) {
        fields.put(fieldName, value);
        return self();
    }

    public T addTagToReport(String tagName, String value) {
        tags.put(tagName, value);
        return self();
    }

    public T setSuccess() {
        addFieldToReport("success", true);
        return self();
    }

    public T setFailed() {
        addFieldToReport("success", false);
        return self();
    }

    public abstract T report();

    protected abstract T self();

}