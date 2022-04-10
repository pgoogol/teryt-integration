package com.pgoogol.teryt.integration.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.pgoogol.teryt.integration.config.properties.IndexConfigProperties;
import com.pgoogol.teryt.integration.model.elk.AddressVersionEntity;
import org.springframework.stereotype.Service;

@Service
public class AddressVersionRepository extends BaseRepositoryOld<AddressVersionEntity> {

    public AddressVersionRepository(IndexConfigProperties indexConfigProperties, ElasticsearchClient client) {
        super(indexConfigProperties.getAddressVersionIndex(), client);
    }



}
