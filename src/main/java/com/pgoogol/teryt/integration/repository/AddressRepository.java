package com.pgoogol.teryt.integration.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.pgoogol.teryt.integration.config.properties.IndexConfigProperties;
import com.pgoogol.teryt.integration.model.elk.AdressesReadEntity;
import org.springframework.stereotype.Service;

@Service
public class AddressRepository extends BaseRepositoryOld<AdressesReadEntity> {

    public AddressRepository(IndexConfigProperties indexConfigProperties, ElasticsearchClient client) {
        super(indexConfigProperties.getAddressIndex(), client);
    }



}
