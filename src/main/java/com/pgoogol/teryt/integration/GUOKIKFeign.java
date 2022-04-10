package com.pgoogol.teryt.integration;

import com.pgoogol.teryt.integration.model.teryt.Territorial;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(url = "https://mapy.geoportal.gov.pl/wss/service/SLN/guest/sln", name = "teryt")
public interface GUOKIKFeign {

    @GetMapping(value = "/woj.json")
    Territorial getWoj();

}
