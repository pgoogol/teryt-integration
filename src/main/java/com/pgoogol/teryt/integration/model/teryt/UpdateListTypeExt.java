package com.pgoogol.teryt.integration.model.teryt;

import com.pgoogol.teryt.integration.wsdl.offline.UpdateListType;

public class UpdateListTypeExt extends UpdateListType {

    private String terytId;

    public UpdateListTypeExt(UpdateListType updateList, String idTeryt) {
        super.verId = updateList.getVerId();
        super.update = updateList.getUpdate();
        this.terytId = idTeryt;
    }

    public String getTerytId() {
        return terytId;
    }

    public void setTerytId(String terytId) {
        this.terytId = terytId;
    }
}
