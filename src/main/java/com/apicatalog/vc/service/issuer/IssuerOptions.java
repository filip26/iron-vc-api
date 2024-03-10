package com.apicatalog.vc.service.issuer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.service.Constants;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

public record IssuerOptions(
        String cryptosuite,
        String curve,
        Instant created,
        String domain,
        String challenge,
        Collection<String> mandatoryPointers) {

    static final String EDDSA_RDFC_2022 = "eddsa-rdfc-2022";
    static final String ECDSA_SD_2023 = "ecdsa-sd-2023";
    static final String ECDSA_RDFC_2019 = "ecdsa-rdfc-2019";
    static final String ED25519_2020 = "Ed25519Signature2020";
    static final String P384 = "p-384";

    static final IssuerOptions getOptions(RoutingContext ctx) throws DocumentError {

        final MultiMap queryParams = ctx.queryParams();

        // suite name
        final String suiteName = queryParams.contains("suite") ? queryParams.get("suite") : "Ed25519Signature2020";
        final String curve = queryParams.contains("curve") ? queryParams.get("curve") : null;

        // clients' options
        var body = ctx.body().asJsonObject();

        var options = body.getJsonObject(Constants.OPTIONS);

        // default values
        Instant created = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        String domain = null;
        String challenge = null;
        Collection<String> mandatoryPointers = null;

        // request options
        if (options != null) {
            created = options.getInstant(Constants.OPTION_CREATED, created);
            domain = options.getString(Constants.OPTION_DOMAIN, null);
            challenge = options.getString(Constants.OPTION_CHALLENGE, null);
            mandatoryPointers = getPointers(options.getJsonArray(Constants.OPTION_MANDATORY_POINTERS));

            var unknown = options.stream()
                    .filter(e -> !Constants.OPTIONS_KEYS.contains(e.getKey()))
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));

            if (unknown != null && !unknown.isBlank()) {
                System.out.println("UNKNOWN OPTIONS [" + unknown + "]");
            }
        }

        return new IssuerOptions(suiteName, curve, created, domain, challenge, mandatoryPointers);
    }

    protected static Collection<String> getPointers(JsonArray input) {
        if (input == null) {
            return Collections.emptyList();
        }

        var pointers = new ArrayList<String>(input.size());

        for (int i = 0; i < input.size(); i++) {
            pointers.add(input.getString(i));
        }
        return pointers;
    }

}
