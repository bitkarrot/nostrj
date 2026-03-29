package org.nostrj.relay.http;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import org.nostrj.relay.config.RelayConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RelayInfoController {
    
    private final RelayConfiguration config;

    public RelayInfoController(RelayConfiguration config) {
        this.config = config;
    }

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Map<String, Object>> getRelayInfo() {
        if (!config.isEnableNip11()) {
            return HttpResponse.notFound();
        }

        Map<String, Object> info = new HashMap<>();
        info.put("name", config.getName());
        info.put("description", config.getDescription());
        info.put("pubkey", "");
        info.put("contact", config.getContact());
        info.put("supported_nips", List.of(1, 9, 11, 12, 15, 16, 20, 22, 33, 40));
        info.put("software", "https://github.com/nostrj/nostrj");
        info.put("version", "0.1.0");

        return HttpResponse.ok(info)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept")
                .header("Access-Control-Allow-Methods", "GET");
    }
}
