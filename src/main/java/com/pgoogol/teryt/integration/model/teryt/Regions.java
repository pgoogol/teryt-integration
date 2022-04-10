package com.pgoogol.teryt.integration.model.teryt;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Regions {

    @JsonProperty(value = "jednAdm")
    private Region terytUnit;

}
