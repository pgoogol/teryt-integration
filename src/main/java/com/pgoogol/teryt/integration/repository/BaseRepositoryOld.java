package com.pgoogol.teryt.integration.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.IdsQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.pgoogol.teryt.integration.model.elk.AddressVersionEntity;
import com.pgoogol.teryt.integration.model.elk.BaseEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.GenericTypeResolver;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BaseRepositoryOld<T extends BaseEntity> {

    protected static final int SIZE = 100;
    protected final String index;
    protected final ElasticsearchClient client;
    private final Logger log = LogManager.getLogger(BaseRepositoryOld.class);
    private final Class<T> clazz;

    @SuppressWarnings("unchecked")
    public BaseRepositoryOld(String index, ElasticsearchClient client) {
        this.clazz = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), BaseRepositoryOld.class);
        this.index = index;
        this.client = client;
    }

    public boolean indexExists() throws IOException {
        BooleanResponse exists = client.indices().exists(builder -> builder.index(index));
        return exists.value();
    }

    public List<T> get() {
        try {
            return client.search(builder -> builder.index(index).size(SIZE), clazz)
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

    public Optional<T> getById(String id) {
        try {
            return Optional.ofNullable(client.get(builder -> builder.index(index).id(id), clazz).source());
        } catch (IOException | ElasticsearchException e) {
            log.error("get error");
        }
        return Optional.empty();
    }

    public Map<String, T> getByIds(List<String> ids) {
        if (!ids.isEmpty()) {
            try {
                return client.search(builder -> builder.index(index)
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

    public void save(AddressVersionEntity item) {
        try {
            client.create(builder -> builder.index(index).document(item).id(item.getId()));
        } catch (IOException | ElasticsearchException e) {
            log.error("Save error", e);
        }
    }

    public void update(AddressVersionEntity item) {
        try {
            client.update(builder -> builder.index(index).id(item.getId()).doc(item), clazz);
        } catch (IOException | ElasticsearchException e) {
            log.error("Save error", e);
        }
    }

    public void saveAll(List<T> items) {
        if (!items.isEmpty()) {
            if (items.size() <= 30000) {
                List<BulkOperation> bulkOperations = new ArrayList<>();
                items.forEach(item -> bulkOperations.add(new BulkOperation.Builder().create(builder -> builder.document(item).id(item.getId())).build()));
                BulkRequest bulkRequest = new BulkRequest.Builder().index(index).operations(bulkOperations).build();
                try {
                    client.bulk(bulkRequest);
                    log.info("Save");
                } catch (IOException | ElasticsearchException e) {
                    log.error("Save error");
                }
            } else {
                for (List<T> ts : partitionListBasedOnSize(items)) {
                    saveAll(ts);
                }
            }
        }
    }

    private List<List<T>> partitionListBasedOnSize(List<T> items) {
        List<List<T>> chunkList = new LinkedList<>();
        for (int i = 0; i < items.size(); i += 30000) {
            chunkList.add(items.subList(i, i + 30000 >= items.size() ? items.size() - 1 : i + 30000));
        }
        return chunkList;
    }

    public void deleteByIds(List<String> ids) {
        if (!ids.isEmpty()) {
            try {
                client.deleteByQuery(builder -> builder.index(index)
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
