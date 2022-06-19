package com.pgoogol.teryt.integration.transformer;

import com.pgoogol.teryt.integration.mapper.AddressMapper;
import com.pgoogol.teryt.integration.model.elk.AddressVersionEntity;
import com.pgoogol.teryt.integration.model.elk.AddressesReadEntity;
import com.pgoogol.teryt.integration.model.teryt.AddressFiles;
import com.pgoogol.teryt.integration.service.ElasticsearchService;
import com.pgoogol.teryt.integration.wsdl.online.Adres;
import com.pgoogol.teryt.integration.wsdl.online.ListaAdresow;
import com.pgoogol.teryt.integration.wsdl.online.ListaMiejscowosc;
import com.pgoogol.teryt.integration.wsdl.online.ListaUlic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.xml.transformer.UnmarshallingTransformer;
import org.springframework.messaging.Message;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ParseFileAndSaveTransformer implements GenericTransformer<Message<AddressFiles>, Message<AddressFiles>> {

    private final Logger log = LogManager.getLogger(ParseFileAndSaveTransformer.class);

    private static final String CITY_CODE_CLASS = "m";
    private static final String STREET_CODE_CLASS = "ul";
    private static final String ADDRESS_CODE_CLASS = "adr";

    private final Jaxb2Marshaller jaxb2Marshaller;
    private final AddressMapper addressMapper;
    private final ElasticsearchService service;

    public ParseFileAndSaveTransformer(Jaxb2Marshaller jaxb2Marshaller,
                                       AddressMapper addressMapper,
                                       ElasticsearchService service
    ) {
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.service = service;
        this.addressMapper = addressMapper;
    }

    @Override
    public Message<AddressFiles> transform(Message<AddressFiles> source) {
        for (File file : source.getPayload().getFiles()) {
            ListaAdresow addressesList = mapFileToObject(file);
            List<String> addressesId = addressesList.getAdres().stream().map(Adres::getPktPrgIIPId).collect(Collectors.toList());
            Map<String, AddressesReadEntity> addresses = service.getByIds(addressesId);
            service.deleteByIds(addressesList);
            saveAddresses(addressesList, addresses);
        }
        updateAddressVersion(source);

        return source;
    }

    private ListaAdresow mapFileToObject(File file) {
        jaxb2Marshaller.setMappedClass(Objects.requireNonNull(setClassObject(file.getName())));
        UnmarshallingTransformer unmarshallingTransformer = new UnmarshallingTransformer(jaxb2Marshaller);
        return (ListaAdresow) unmarshallingTransformer.transformPayload(file);
    }

    private Class<?> setClassObject(String name) {
        String[] split = name.split(File.separator);
        switch (split[2]) {
            case CITY_CODE_CLASS:
                return ListaMiejscowosc.class;
            case STREET_CODE_CLASS:
                return ListaUlic.class;
            case ADDRESS_CODE_CLASS:
                return ListaAdresow.class;
            default:
                return null;
        }
    }

    private void saveAddresses(ListaAdresow addressesList, Map<String, AddressesReadEntity> byIds) {
        List<AddressesReadEntity> saveAddresses = new LinkedList<>();
        addressesList.getAdres()
                .stream()
                .filter(place -> place.getCyklZyciaDo() == null)
                .forEach(address -> {
                    AddressesReadEntity newAddress;
                    if (byIds != null && byIds.containsKey(address.getPktPrgIIPId())) {
                        newAddress = byIds.get(address.getPktPrgIIPId());
                        addressMapper.updateDocument(address, newAddress);
                    } else {
                        newAddress = addressMapper.soupToIndex(address);
                    }
                    saveAddresses.add(newAddress);
                });

        service.saveAll(saveAddresses);
    }

    private void updateAddressVersion(Message<AddressFiles> source) {
        Optional<AddressVersionEntity> byId = service.getById(source.getPayload().getTerytId());
        if (byId.isPresent()) {
            AddressVersionEntity addressVersionEntity = byId.get();
            addressVersionEntity.setVerId(source.getPayload().getVerId());
            service.update(addressVersionEntity);
        } else {
            service.save(
                    AddressVersionEntity.builder()
                            .verId(source.getPayload().getVerId())
                            .idTeryt(source.getPayload().getTerytId())
                            .build()
            );
        }
    }

}
