package com.apicatalog.vc.service.issuer;

import com.apicatalog.vc.service.Constants;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;


public class IssuerApi extends AbstractVerticle {

    public static void setup(Router router) throws Exception {

        // issues a credential and returns the signed credentials in the response body
        router
            .post("/credentials/issue")
            .consumes("application/ld+json")
            .consumes("application/json")
            .produces("application/ld+json")
            .produces("application/json")
            .putMetadata(Constants.CTX_DOCUMENT_KEY, Constants.CREDENTIAL_KEY)
            
            //FIXME remove
            .handler(ctx -> {
                System.out.println(ctx.body().asString());
                ctx.next();
            })
            
            // issue
            .blockingHandler(new IssuingHandler())

            // handle errors
            .failureHandler(new IssuerErrorHandler());
    }
}
