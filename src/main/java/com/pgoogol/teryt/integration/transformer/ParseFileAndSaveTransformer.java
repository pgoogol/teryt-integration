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
        log.debug(String.format("start parsed version %s", source.getPayload().getVerId()));
        for (File file : source.getPayload().getFiles()) {
            ListaAdresow addressesList = mapFileToObject(file);
            List<String> addressesId = addressesList.getAdres().stream().map(Adres::getPktPrgIIPId).collect(Collectors.toList());
            Map<String, AddressesReadEntity> addresses = service.getByIds(addressesId);
            service.deleteByIds(addressesList);
            saveAddresses(addressesList, addresses);
        }
        updateAddressVersion(source);
        log.debug(String.format("end parsed version %s", source.getPayload().getVerId()));

        return source;
    }

    private ListaAdresow mapFileToObject(File file) {
        jaxb2Marshaller.setMappedClass(Objects.requireNonNull(setClassObject(file.getName())));
        UnmarshallingTransformer unmarshallingTransformer = new UnmarshallingTransformer(jaxb2Marshaller);
        return (ListaAdresow) unmarshallingTransformer.transformPayload(file);
    }

    private Class<?> setClassObject(String name) {
        String[] split = name.split("-");
        switch (split[2]) {
            case "m":
                return ListaMiejscowosc.class;
            case "ul":
                return ListaUlic.class;
            case "adr":
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
        log.info(String.format("addressesList size %d", saveAddresses.size()));

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
