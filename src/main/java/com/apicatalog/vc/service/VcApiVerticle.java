package com.apicatalog.vc.service;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

import com.apicatalog.vc.service.holder.HolderApi;
import com.apicatalog.vc.service.issuer.IssuerApi;
import com.apicatalog.vc.service.verifier.VerifierApi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class VcApiVerticle extends AbstractVerticle {

    Instant startTime;

    @Override
    public void start() throws Exception {

        final Router router = Router.router(vertx);

        router.post().handler(BodyHandler.create().setBodyLimit(250000));

        // verifier's VC API
        VerifierApi.setup(router);

        // issuer's VC API
        IssuerApi.setup(router);

        // holder's VC API
        HolderApi.setup(router);

        // static resources
        router
                .get("/key/*")
                .handler(StaticHandler
                        .create("webroot/key/")
                        .setIncludeHidden(false)
                        .setDefaultContentEncoding("UTF-8")
                        .setMaxAgeSeconds(365 * 24 * 3600l));

        router.get().handler(StaticHandler
                .create()
                .setIncludeHidden(false)
                .setDefaultContentEncoding("UTF-8")
                .setMaxAgeSeconds(4 * 3600l) // maxAge = 4 hours
        );

        // server options
        var serverOptions = new HttpServerOptions()
                .setMaxWebSocketFrameSize(1000000)
                .setUseAlpn(true);

        // service
        vertx
                .createHttpServer(serverOptions)
                .requestHandler(router)
                .listen(getDefaultPort())
                .onSuccess(ctx -> {
                    System.out.println(VcApiVerticle.class.getName() + " started on port " + ctx.actualPort() + " with " + Charset.defaultCharset() + " charset.");
                    startTime = Instant.now();
                })
                .onFailure(ctx -> System.err.println(VcApiVerticle.class.getName() + " start failed [" + ctx.getMessage() + "]."));
    }

    @Override
    public void stop() throws Exception {
        if (startTime != null) {
            System.out.println(VcApiVerticle.class.getName() + " stopped after running for " + Duration.between(startTime, Instant.now()) + ".");
        }
    }

    static final int getDefaultPort() {
        final String envPort = System.getenv("PORT");

        if (envPort != null) {
            return Integer.valueOf(envPort);
        }
        return 8080;
    }
}
