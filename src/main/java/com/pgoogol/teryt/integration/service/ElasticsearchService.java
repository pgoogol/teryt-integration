package com.pgoogol.teryt.integration.service;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.pgoogol.elasticsearch.data.repository.ElasticsearchRepository;
import com.pgoogol.teryt.integration.config.properties.IndexConfigProperties;
import com.pgoogol.teryt.integration.model.elk.AddressVersionEntity;
import com.pgoogol.teryt.integration.model.elk.AddressesReadEntity;
import com.pgoogol.teryt.integration.model.elk.BaseEntity;
import com.pgoogol.teryt.integration.wsdl.online.Adres;
import com.pgoogol.teryt.integration.wsdl.online.ListaAdresow;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    protected static final int SIZE = 100;

    private final ElasticsearchRepository repository;
    private final IndexConfigProperties indexConfigProperties;

    public ElasticsearchService(ElasticsearchRepository repository, IndexConfigProperties indexConfigProperties) {
        this.repository = repository;
        this.indexConfigProperties = indexConfigProperties;
    }

    public Map<String, AddressVersionEntity> getAllTerytVersion() {
        return repository.getAll(
                        indexConfigProperties.getAddressVersionIndex(),
                        PageRequest.of(0, SIZE),
                        AddressVersionEntity.class
                ).hits()
                .stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        BaseEntity::getId,
                        Function.identity()
                ));
    }

    public Optional<AddressVersionEntity> getById(@NotBlank String id) {
        return repository.getById(
                indexConfigProperties.getAddressVersionIndex(),
                id,
                AddressVersionEntity.class
        );
    }

    public Map<String, AddressesReadEntity> getByIds(@NotEmpty List<String> ids) {
        return repository.getByIds(
                indexConfigProperties.getAddressIndex(),
                ids,
                AddressesReadEntity.class
        ).orElse(Collections.emptyList())
        .stream()
        .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
    }

    public AddressVersionEntity save(AddressVersionEntity item) {
        return repository.save(indexConfigProperties.getAddressIndex(), item.getVerId(), item);
    }

    public List<AddressesReadEntity> saveAll(List<AddressesReadEntity> items) {
        return repository.saveAll(indexConfigProperties.getAddressIndex(), AddressesReadEntity::getId, items);
    }

    public AddressVersionEntity update(AddressVersionEntity item) {
        return repository.update(indexConfigProperties.getAddressVersionIndex(), item.getId(), item);
    }

    public void deleteByIds(ListaAdresow listaAdresow) {
        List<String> ids = listaAdresow.getAdres()
                .stream()
                .filter(place -> place.getCyklZyciaDo() != null)
                .map(Adres::getPktPrgIIPId)
                .collect(Collectors.toList());
        repository.delete(indexConfigProperties.getAddressIndex(), ids);
    }

}
