package no.nav.sbl.dialogarena.common.abac.pep;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import no.nav.sbl.dialogarena.common.abac.pep.domain.request.XacmlRequest;
import no.nav.sbl.dialogarena.common.abac.pep.domain.response.*;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;


public class XacmlMapper {
    private static final Gson gsonUnserialize;
    private static final Gson gsonSerialize;

    static {
        Type responseType = new TypeToken<List<Response>>() {
        }.getType();
        Type associatedAdviceType = new TypeToken<List<Advice>>() {
        }.getType();
        Type attributeAssignmentType = new TypeToken<List<AttributeAssignment>>() {
        }.getType();

        gsonUnserialize = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .registerTypeAdapter(responseType, new ResponseTypeAdapter())
                .registerTypeAdapter(associatedAdviceType, new AssociatedAdviceTypeAdapter())
                .registerTypeAdapter(attributeAssignmentType, new AttributeAssignmentTypeAdapter())
                .create();

        gsonSerialize = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setPrettyPrinting()
                .create();
    }

    public static XacmlResponse mapRawResponse(String content) throws IOException {
        return gsonUnserialize.fromJson(content, XacmlResponse.class);
    }

    public static StringEntity mapRequestToEntity(XacmlRequest request) throws UnsupportedEncodingException {
        return new StringEntity(gsonSerialize.toJson(request));
    }
}
