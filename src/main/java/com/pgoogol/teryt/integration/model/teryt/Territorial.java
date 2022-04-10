package com.pgoogol.teryt.integration.model.teryt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Territorial {

    @JsonProperty(value = "jednAdms")
    private List<Regions> regionsList;

}
