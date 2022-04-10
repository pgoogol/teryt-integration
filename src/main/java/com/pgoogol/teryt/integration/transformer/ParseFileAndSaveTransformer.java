package com.pgoogol.teryt.integration.transformer;

import com.pgoogol.teryt.integration.model.elk.AddressVersionEntity;
import com.pgoogol.teryt.integration.model.elk.AdressesReadEntity;
import com.pgoogol.teryt.integration.repository.AddressRepository;
import com.pgoogol.teryt.integration.repository.AddressVersionRepository;
import com.pgoogol.teryt.integration.wsdl.online.ListaUlic;
import com.pgoogol.teryt.integration.mapper.AddressMapper;
import com.pgoogol.teryt.integration.model.teryt.AddressFiles;
import com.pgoogol.teryt.integration.wsdl.online.Adres;
import com.pgoogol.teryt.integration.wsdl.online.ListaAdresow;
import com.pgoogol.teryt.integration.wsdl.online.ListaMiejscowosc;
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
    private final AddressRepository addressRepository;
    private final AddressVersionRepository addressVersionRepository;

    public ParseFileAndSaveTransformer(Jaxb2Marshaller jaxb2Marshaller,
                                       AddressMapper addressMapper,
                                       AddressRepository addressRepository,
                                       AddressVersionRepository addressVersionRepository) {
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
        this.addressVersionRepository = addressVersionRepository;
    }

    @Override
    public Message<AddressFiles> transform(Message<AddressFiles> source) {
        log.info(String.format("start parsed version %s", source.getPayload().getVerId()));
        for (File file : source.getPayload().getFiles()) {
            ListaAdresow addressesList = mapFileToObject(file);
            List<String> addressesId = addressesList.getAdres().stream().map(Adres::getPktPrgIIPId).collect(Collectors.toList());
            Map<String, AdressesReadEntity> addresses = addressRepository.getByIds(addressesId);
            removeAddresses(addressesList);
            saveAddresses(addressesList, addresses);
        }
        updateAddressVersion(source);
        log.info(String.format("end parsed version %s", source.getPayload().getVerId()));

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

    private void removeAddresses(ListaAdresow addressesList) {
        addressRepository.deleteByIds(
                addressesList.getAdres()
                             .stream()
                             .filter(place -> place.getCyklZyciaDo() != null)
                             .map(Adres::getPktPrgIIPId)
                             .collect(Collectors.toList())
        );
    }

    private void saveAddresses(ListaAdresow addressesList, Map<String, AdressesReadEntity> byIds) {
        List<AdressesReadEntity> saveAddresses = new LinkedList<>();
        addressesList.getAdres()
         .stream()
         .filter(place -> place.getCyklZyciaDo() == null)
         .forEach(address -> {
            AdressesReadEntity newAddress;
            if (byIds != null && byIds.containsKey(address.getPktPrgIIPId())) {
                newAddress = byIds.get(address.getPktPrgIIPId());
                addressMapper.updateDocument(address, newAddress);
            } else {
                newAddress = addressMapper.soupToIndex(address);
            }
             saveAddresses.add(newAddress);
        });
        log.info(String.format("addressesList size %d", saveAddresses.size()));
        addressRepository.saveAll(saveAddresses);
    }

    private void updateAddressVersion(Message<AddressFiles> source) {
        Optional<AddressVersionEntity> byId = addressVersionRepository.getById(source.getPayload().getTerytId());
        if (byId.isPresent()) {
            AddressVersionEntity addressVersionEntity = byId.get();
            addressVersionEntity.setVerId(source.getPayload().getVerId());
            addressVersionRepository.update(addressVersionEntity);
        } else {
            addressVersionRepository.save(
                    AddressVersionEntity.builder()
                    .verId(source.getPayload().getVerId())
                    .idTeryt(source.getPayload().getTerytId())
                    .build()
            );
        }

    }

}
