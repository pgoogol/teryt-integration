package com.pgoogol.teryt.integration.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.IdsQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.pgoogol.teryt.integration.config.properties.IndexConfigProperties;
import com.pgoogol.teryt.integration.model.elk.AdressesReadEntity;
import com.pgoogol.teryt.integration.model.elk.BaseEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class AddressRepository {

    private final Logger log = LogManager.getLogger(AddressRepository.class);

    private final ElasticsearchClient client;
    private final Class<AdressesReadEntity> clazz = AdressesReadEntity.class;
    private final IndexConfigProperties indexConfigProperties;

    public AddressRepository(IndexConfigProperties indexConfigProperties, ElasticsearchClient client) {
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
        BooleanResponse exists = client.indices().exists(builder -> builder.index(indexConfigProperties.getAddressIndex()));
        return exists.value();
    }

    public void indexCreate() throws IOException {
        client.indices().create(builder -> builder.index(indexConfigProperties.getAddressIndex()));
    }

    public Map<String, AdressesReadEntity> getByIds(List<String> ids) {
        if (!ids.isEmpty()) {
            try {
                return client.search(builder -> builder.index(indexConfigProperties.getAddressIndex())
                                .query(queryBuilder -> queryBuilder.ids(
                                        IdsQuery.of(idsBuilder -> idsBuilder.values(ids)))
                                ), clazz)
                        .documents()
                        .stream()
                        .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));
            } catch (IOException | ElasticsearchException e) {
                log.error("get error");
            }
        }
        return Collections.emptyMap();
    }

    public void saveAll(List<AdressesReadEntity> items) {
        if (!items.isEmpty()) {
            if (items.size() <= 30000) {
                List<BulkOperation> bulkOperations = new ArrayList<>();
                items.forEach(
                        item -> bulkOperations.add(
                                new BulkOperation.Builder()
                                        .create(builder -> builder.document(item).id(item.getId()))
                                        .build()
                        )
                );
                BulkRequest bulkRequest = new BulkRequest.Builder()
                        .index(indexConfigProperties.getAddressIndex())
                        .operations(bulkOperations)
                        .build();
                try {
                    client.bulk(bulkRequest);
                    log.info("Save");
                } catch (IOException | ElasticsearchException e) {
                    log.error("Save error");
                }
            } else {
                for (List<AdressesReadEntity> ts : partitionListBasedOnSize(items)) {
                    saveAll(ts);
                }
            }
        }
    }

    private List<List<AdressesReadEntity>> partitionListBasedOnSize(List<AdressesReadEntity> items) {
        List<List<AdressesReadEntity>> chunkList = new LinkedList<>();
        for (int i = 0; i < items.size(); i += 30000) {
            chunkList.add(items.subList(i, i + 30000 >= items.size() ? items.size() - 1 : i + 30000));
        }
        return chunkList;
    }

    public void deleteByIds(List<String> ids) {
        if (!ids.isEmpty()) {
            try {
                client.deleteByQuery(builder -> builder.index(indexConfigProperties.getAddressIndex())
                        .query(queryBuilder -> queryBuilder.ids(
                                IdsQuery.of(idsBuilder -> idsBuilder.values(ids))
                        ))
                );
            } catch (IOException | ElasticsearchException e) {
                log.error("delete error");
            }
        }
    }


}
