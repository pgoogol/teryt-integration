package com.pgoogol.teryt.integration.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.pgoogol.teryt.integration.config.properties.IndexConfigProperties;
import com.pgoogol.teryt.integration.model.elk.AddressVersionEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AddressVersionRepository {

    private final Logger log = LogManager.getLogger(AddressVersionRepository.class);

    protected static final int SIZE = 100;

    private final ElasticsearchClient client;
    private final Class<AddressVersionEntity> clazz = AddressVersionEntity.class;
    private final IndexConfigProperties indexConfigProperties;

    public AddressVersionRepository(IndexConfigProperties indexConfigProperties, ElasticsearchClient client) {
        this.indexConfigProperties = indexConfigProperties;
        this.client = client;
    }

    @PostConstruct
    public void init() {
        try {
            if (!indexExists()) {
                indexCreate();
            }
        } catch (IOException | ElasticsearchException e) {
            log.error("create error");
        }
    }

    public boolean indexExists() throws IOException {
        BooleanResponse exists = client.indices().exists(builder -> builder.index(indexConfigProperties.getAddressVersionIndex()));
        return exists.value();
    }

    public void indexCreate() throws IOException {
        client.create(builder -> builder.index(indexConfigProperties.getAddressVersionIndex()));
    }

    public List<AddressVersionEntity> get() {
        try {
            return client.search(
                        builder -> builder.index(indexConfigProperties.getAddressVersionIndex()).size(SIZE),
                        clazz
                    )
                    .hits()
                    .hits()
                    .stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException | ElasticsearchException e) {
            log.error("get error");
        }
        return Collections.emptyList();
    }

    public Optional<AddressVersionEntity> getById(String id) {
        try {
            return Optional.ofNullable(
                        client.get(builder -> builder.index(indexConfigProperties.getAddressVersionIndex()).id(id),
                        clazz
                    ).source());
        } catch (IOException | ElasticsearchException e) {
            log.error("get error");
        }
        return Optional.empty();
    }

    public void save(AddressVersionEntity item) {
        try {
            client.create(
                    builder -> builder.index(indexConfigProperties.getAddressVersionIndex())
                            .document(item)
                            .id(item.getId())
            );
        } catch (IOException | ElasticsearchException e) {
            log.error("Save error", e);
        }
    }

    public void update(AddressVersionEntity item) {
        try {
            client.update(
                builder -> builder.index(indexConfigProperties.getAddressVersionIndex()).id(item.getId()).doc(item),
                clazz
            );
        } catch (IOException | ElasticsearchException e) {
            log.error("update error", e);
        }
    }

}
