package com.pgoogol.teryt.integration.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.IdsQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.TransportException;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.pgoogol.teryt.integration.config.properties.IndexConfigProperties;
import com.pgoogol.teryt.integration.model.elk.AdressesReadEntity;
import com.pgoogol.teryt.integration.model.elk.AddressVersionEntity;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BaseRepository {

    private final Logger log = LogManager.getLogger(BaseRepository.class);

    protected final ElasticsearchClient client;
    protected final IndexConfigProperties indexConfigProperties;

    public BaseRepository(IndexConfigProperties indexConfigProperties, ElasticsearchClient client) {
        this.client = client;
        this.indexConfigProperties = indexConfigProperties;
    }

    @PostConstruct
    private void init() throws IOException {
       //if (!indexExists()) {
       //    createIndex();
       //}
        //createIndexVersion();

        /*AnalyzeRequest analyzeRequest = new AnalyzeRequest.Builder().index(indexConfigProperties.getAddressIndex())
                .tokenizer(tokenizer -> tokenizer.whitespace(whitespaceTokenizer -> whitespaceTokenizer.maxTokenLength(20)))
                .addFilter(tokenFilter -> tokenFilter.lowercase(lowercaseToken -> lowercaseToken.language("polish"))
                                                    .asciifolding(asciiFolding -> asciiFolding.preserveOriginal(false)))
                .build();
        TokenFilter autocompleatFilter = new TokenFilter.Builder().ngram(ngram -> ngram.minGram(2).maxGram(20)).build();
        AnalyzeRequest autocompleatAnalyzeRequest = new AnalyzeRequest.Builder().index(indexConfigProperties.getAddressIndex())
                .tokenizer(tokenizer -> tokenizer.whitespace(whitespaceTokenizer -> whitespaceTokenizer.maxTokenLength(20)))
                .addFilter(tokenFilter -> tokenFilter.lowercase(lowercaseToken -> lowercaseToken.language("polish"))
                                                    .asciifolding(asciiFolding -> asciiFolding.preserveOriginal(false))
                )
                .addFilter(autocompleatFilter)
                .build();

        client.indices().analyze(analyzeRequest);
        client.indices().analyze(autocompleatAnalyzeRequest);*/

        //PutMappingRequest putMappingRequest = new PutMappingRequest.Builder().index(indexConfigProperties.getAddressIndex())
          //              .putProperties("", new Property(builder -> builder.text(builder1 -> builder1.analyzer())))
       // client.indices().putMapping()
    }

    public boolean indexExists() throws IOException {
        BooleanResponse exists = client.indices().exists(builder -> builder.index(indexConfigProperties.getAddressIndex()));
        return exists.value();
        //return client.exists(b -> b.index(indexConfigProperties.getAddressIndex()).id("")).value();
    }

    @SneakyThrows
    public List<AddressVersionEntity> getAddressVer() {
        List<AddressVersionEntity> documents = new ArrayList<>();
        try {
            documents = client.search(builder -> builder.index(indexConfigProperties.getAddressVersionIndex()).size(20), AddressVersionEntity.class)
                    .hits().hits().stream().map(Hit::source).collect(Collectors.toList());
        } catch (TransportException e) {

        }
        return documents;
    }

    @SneakyThrows
    public boolean isNotEmpty() {
        return client.count(builder -> builder.index(indexConfigProperties.getAddressIndex())).count() != 0;
        

    }

    private boolean createIndex() throws IOException {
        return client.indices().create(builder -> builder.index(indexConfigProperties.getAddressIndex())).acknowledged();
        /*return client.indices().create(b -> b.index(indexConfigProperties.getAddressIndex())
                .putSettings("max_ngram_diff", JsonData.of(19))
                .putSettings("number_of_shards", JsonData.of(indexConfigProperties.getShardsNumber()))
                .putSettings("number_of_replicas", JsonData.of(indexConfigProperties.getReplicasNumber()))
        ).acknowledged();*/
    }

    private void createIndexVersion() throws IOException {
        client.indices().create(builder -> builder.index(indexConfigProperties.getAddressVersionIndex())).acknowledged();
    }

    @SneakyThrows
    public Map<String, AdressesReadEntity> getByIds(List<String> ids) {
        return client.search(builder -> builder.index(indexConfigProperties.getAddressIndex())
                                        .query(queryBuilder -> queryBuilder.ids(
                                                        IdsQuery.of(idsBuilder -> idsBuilder.values(ids)))
                                        ), AdressesReadEntity.class)
                .documents()
                .stream()
                .collect(Collectors.toMap(AdressesReadEntity::getId, Function.identity()));
    }

    @SneakyThrows
    public void deleteByIds(List<String> ids) {
        client.deleteByQuery(builder -> builder.index(indexConfigProperties.getAddressIndex())
                                               .query(queryBuilder -> queryBuilder.ids(
                                                       IdsQuery.of(idsBuilder -> idsBuilder.values(ids))
                                               ))
        );
    }

    public void create(List<AdressesReadEntity> items) throws IOException {
        if (!items.isEmpty()) {
            List<BulkOperation> bulkOperations = new ArrayList<>();
            items.forEach(item -> bulkOperations.add(new BulkOperation.Builder().create(builder -> builder.document(item).id(item.getId())).build()));
            BulkRequest bulkRequest = new BulkRequest.Builder().index(indexConfigProperties.getAddressIndex()).operations(bulkOperations).build();
            try {
                Long aLong = client.bulk(bulkRequest).took();
                System.out.println(aLong);
            } catch (TransportException e) {

            }
        }
    }

    public void createVersion(AddressVersionEntity item) throws IOException {
        client.create(builder -> builder.index(indexConfigProperties.getAddressVersionIndex()).document(item).id(item.getIdTeryt()));
    }

    public void createVersions(List<AddressVersionEntity> items) throws IOException {
        if (!items.isEmpty()) {
            List<BulkOperation> bulkOperations = new ArrayList<>();
            items.forEach(item -> bulkOperations.add(new BulkOperation.Builder().create(builder -> builder.document(item).id(item.getIdTeryt())).build()));
            BulkRequest bulkRequest = new BulkRequest.Builder().index(indexConfigProperties.getAddressVersionIndex()).operations(bulkOperations).build();
            try {
                Long aLong = client.bulk(bulkRequest).ingestTook();
                System.out.println(aLong);
            } catch (TransportException e) {

            }
        }
    }

}
