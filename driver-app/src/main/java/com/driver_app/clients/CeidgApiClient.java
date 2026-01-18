package com.driver_app.clients;

import com.driver_app.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ceidgApi", url = "${ceidg.api.url}", configuration = FeignConfig.class)
public interface CeidgApiClient {
    @GetMapping("/firma")
    CeidgResponse getCompanyInfo(@RequestParam("nip") String nip);
}