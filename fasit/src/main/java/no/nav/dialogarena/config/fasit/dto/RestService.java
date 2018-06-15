package no.nav.dialogarena.config.fasit.dto;

import lombok.Builder;
import lombok.Value;

import javax.ws.rs.core.GenericType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class RestService {

    private String url;
    private String alias;
    private String environment;
    private String environmentClass;
    private String application;

}
