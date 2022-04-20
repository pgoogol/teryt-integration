package com.pgoogol.teryt.integration.model.teryt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Region {

    @JsonProperty(value = "wojNazwa")
    private String regionName;

    @JsonProperty(value = "wojIdTeryt")
    private String regionIdTeryt;

}

