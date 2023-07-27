package com.apicatalog.vc.service.issuer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.service.Constants;

import io.vertx.ext.web.RoutingContext;

public record IssuerOptions(
        String cryptosuite,
        Instant created,
        String domain,
        String challenge
        ) {

    static final IssuerOptions getOptions(RoutingContext ctx) throws DocumentError {

        var body = ctx.body().asJsonObject();

        var options = body.getJsonObject(Constants.OPTIONS);

        // default values
        Instant created = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        String domain = null;
        String challenge = null;

        // suite name
        String suiteName = "Ed25519Signature2020";

        // request options
        if (options != null) {
            suiteName = options.getString(Constants.OPTION_TYPE, suiteName);
            created = options.getInstant(Constants.OPTION_CREATED, created);
            domain = options.getString(Constants.OPTION_DOMAIN, null);
            challenge = options.getString(Constants.OPTION_CHALLENGE, null);
        }

        return new IssuerOptions(suiteName, created, domain, challenge);
    }

}