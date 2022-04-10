package com.pgoogol.teryt.integration.model.elk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.beans.Transient;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressVersionEntity implements BaseEntity {

    private String idTeryt;
    private String verId;

    @Override
    @Transient
    public String getId() {
        return idTeryt;
    }
}
