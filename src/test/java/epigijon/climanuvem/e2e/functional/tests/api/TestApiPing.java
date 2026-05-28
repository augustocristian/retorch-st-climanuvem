package epigijon.climanuvem.e2e.functional.tests.api;

import com.google.gson.JsonObject;
import epigijon.climanuvem.e2e.functional.common.BaseApiClass;
import giis.retorch.annotations.AccessMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Validates the public health-check endpoints that require no authentication:
 * <ul>
 *   <li>GET /ping  — liveness probe</li>
 *   <li>GET /      — root welcome response</li>
 * </ul>
 */
class TestApiPing extends BaseApiClass {

    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("GET /ping returns HTTP 200 with ping:pong payload")
    void testPingEndpoint() throws IOException {
        Assertions.assertEquals(200, getStatus(rootUrl("/ping")), "Expected HTTP 200 from /ping");

        JsonObject body = getJsonObject(rootUrl("/ping"));
        Assertions.assertTrue(body.has("ping"), "/ping response must have 'ping' field");
        Assertions.assertEquals("pong", body.get("ping").getAsString(), "'ping' field must equal 'pong'");
    }

    @AccessMode(resID = "backend", concurrency = 10, sharing = true, accessMode = "READONLY")
    @Test
    @DisplayName("GET / returns HTTP 200 with Hello:World payload")
    void testRootEndpoint() throws IOException {
        Assertions.assertEquals(200, getStatus(rootUrl("/")), "Expected HTTP 200 from /");

        JsonObject body = getJsonObject(rootUrl("/"));
        Assertions.assertTrue(body.has("Hello"), "Root response must have 'Hello' field");
        Assertions.assertEquals("World", body.get("Hello").getAsString(), "'Hello' field must equal 'World'");
    }
}
