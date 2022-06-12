package com.pgoogol.teryt.integration.mapper;

import com.pgoogol.teryt.integration.model.elk.AddressesReadEntity;
import com.pgoogol.teryt.integration.wsdl.online.Adres;
import com.pgoogol.teryt.integration.wsdl.online.Miejscowosc;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface AddressMapper {

    @Mapping(source = "miejscIIPId", target = "id")
    AddressesReadEntity soupToIndex(Miejscowosc source);

    @Mapping(source = "pktPrgIIPId", target = "id")
    AddressesReadEntity soupToIndex(Adres source);

    void updateDocument(Adres source, @MappingTarget AddressesReadEntity dest);

}
