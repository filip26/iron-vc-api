package com.apicatalog.vc.service.verifier;

import java.io.StringReader;
import java.util.HashMap;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.StringUtils;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.ed25519.Ed25519ContextLoader;
import com.apicatalog.vc.integrity.DataIntegrityVocab;
import com.apicatalog.vc.service.Constants;
import com.apicatalog.vc.service.Suites;
import com.apicatalog.vc.verifier.Verifier;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

class VerificationHandler implements Handler<RoutingContext> {

    static final Verifier VERIFIER = Verifier.with(Suites.ALL)
            .loader(new Ed25519ContextLoader(SchemeRouter.defaultInstance()));
    
    @Override
    public void handle(RoutingContext ctx) {

        // set verification result
        var verificationResult = new VerificationResult();
        ctx.put(Constants.CTX_RESULT, verificationResult);

        var route = ctx.currentRoute();

        var document = ctx.body().asJsonObject();

        final String documentKey = route.getMetadata(Constants.CTX_DOCUMENT_KEY);

        if (StringUtils.isNotBlank(documentKey)) {
            var value  = document.getValue(documentKey);
            if (value instanceof JsonArray array) {
                // ignore key length type for verification 
                // keyType = array.getString(0);
                document = array.getJsonObject(1);
                
            } else if (value instanceof JsonObject object) {
                document = object;
            }
        }

        if (document == null) {
            ctx.fail(new DocumentError(ErrorType.Invalid));
            return;
        }

        try {
            verificationResult.addCheck("PROOF");
            
            var params = new HashMap<String, Object>();
            params.put(DataIntegrityVocab.DOMAIN.name(), ctx.get(Constants.OPTION_DOMAIN, null));
            params.put(DataIntegrityVocab.CHALLENGE.name(), ctx.get(Constants.OPTION_CHALLENGE, null));
            params.put(DataIntegrityVocab.PURPOSE.name(), ctx.get(Constants.OPTION_PURPOSE, null));

            // assert document validity
            VERIFIER.verify(JsonDocument.of(new StringReader(document.toString()))
                    .getJsonContent()
                    .orElseThrow(IllegalStateException::new)
                    .asJsonObject(),
                    params
                    );

            ctx.json(verificationResult);

        } catch (JsonLdError | VerificationError | DocumentError e) {
            ctx.fail(e);
        }
    }

}
