package no.nav.sbl.dialogarena.common.abac.pep;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.Wither;
import no.nav.sbl.dialogarena.common.abac.pep.domain.ResourceType;
import no.nav.sbl.dialogarena.common.abac.pep.domain.request.Action;

@Getter
@Wither
@EqualsAndHashCode
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class RequestData {

    private String oidcToken;
    private String samlToken;
    private String subjectId;
    private String domain;
    private AbacPersonId personId;
    private String enhet;
    private Action.ActionId action = Action.ActionId.READ;
    private ResourceType resourceType;
    private String credentialResource;
}