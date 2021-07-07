package no.nav.sbl.dialogarena.common.abac.pep.domain;


import no.nav.sbl.dialogarena.common.abac.pep.NavAttributter;
import no.nav.sbl.dialogarena.common.abac.pep.AbacPersonId;
import no.nav.sbl.dialogarena.common.abac.pep.RequestData;
import no.nav.sbl.dialogarena.common.abac.pep.domain.request.Resource;

public class Resources {

    public static Resource makeEnhetResource(RequestData requestData) {
        Resource resource = new Resource();
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, NavAttributter.RESOURCE_VEILARB_ENHET_EIENDEL));
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_DOMENE, requestData.getDomain()));
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_VEILARB_KONTOR_LAAS, requestData.getEnhet()));
        return resource;
    }

    public static Resource makePersonResource(RequestData requestData) {
        Resource resource = new Resource();
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, NavAttributter.RESOURCE_FELLES_PERSON));
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_DOMENE, requestData.getDomain()));
        resource.getAttribute().add(personIdAttribute(requestData.getPersonId()));
        return resource;
    }

    public static Resource makeKode7Resource(RequestData requestData) {
        Resource resource = new Resource();
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, NavAttributter.SUBJECT_FELLES_HAR_TILGANG_KODE_7));
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_DOMENE, requestData.getDomain()));
        return resource;

    }

    public static Resource makeKode6Resource(RequestData requestData) {
        Resource resource = new Resource();
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, NavAttributter.SUBJECT_FELLES_HAR_TILGANG_KODE_6));
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_DOMENE, requestData.getDomain()));
        return resource;
    }

    public static Resource makeEgenAnsattResource(RequestData requestData) {
        Resource resource = new Resource();
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, NavAttributter.SUBJECT_FELLES_HAR_TILGANG_EGEN_ANSATT));
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_DOMENE, requestData.getDomain()));
        return resource;
    }

    public static Resource makeVeilArbResource(RequestData requestData) {
        Resource resource = new Resource();
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, NavAttributter.RESOURCE_VEILARB));
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_DOMENE, requestData.getDomain()));
        return resource;
    }

    public static Resource makeModiaResource(RequestData requestData) {
        Resource resource = new Resource();
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, NavAttributter.RESOURCE_MODIA));
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_DOMENE, requestData.getDomain()));
        return resource;
    }

    public static Resource makeVeilArbPersonResource(RequestData requestData) {
        Resource resource = new Resource();
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, NavAttributter.RESOURCE_VEILARB_PERSON));
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_DOMENE, requestData.getDomain()));
        resource.getAttribute().add(personIdAttribute(requestData.getPersonId()));
        return resource;
    }

    public static Resource makeVeilArbUnderOppfolgingResource(RequestData requestData) {
        Resource resource = new Resource();
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE, NavAttributter.RESOURCE_VEILARB_UNDER_OPPFOLGING));
        resource.getAttribute().add(new Attribute(NavAttributter.RESOURCE_FELLES_DOMENE, requestData.getDomain()));
        resource.getAttribute().add(personIdAttribute(requestData.getPersonId()));
        return resource;
    }

    private static Attribute personIdAttribute(AbacPersonId personId) {
        switch (personId.getType()) {
            case FNR:
                return new Attribute(NavAttributter.RESOURCE_FELLES_PERSON_FNR, personId.getId());
            case AKTOR_ID:
                return new Attribute(NavAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, personId.getId());
            default:
                throw new IllegalStateException("Ukjent verdi for person id type: " + personId.getType());
        }
    }
}