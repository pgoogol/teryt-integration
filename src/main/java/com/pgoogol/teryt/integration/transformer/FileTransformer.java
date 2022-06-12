package com.pgoogol.teryt.integration.transformer;

import com.pgoogol.teryt.integration.model.elk.AddressVersionEntity;
import com.pgoogol.teryt.integration.model.teryt.Region;
import com.pgoogol.teryt.integration.model.teryt.Regions;
import com.pgoogol.teryt.integration.model.teryt.Territorial;
import com.pgoogol.teryt.integration.model.teryt.UpdateListTypeExt;
import com.pgoogol.teryt.integration.service.ElasticsearchService;
import com.pgoogol.teryt.integration.service.GUOKIKClient;
import com.pgoogol.teryt.integration.wsdl.offline.UpdateListType;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FileTransformer implements GenericTransformer<Message<Territorial>, Message<List<UpdateListTypeExt>>> {

    private final GUOKIKClient client;
    private final ElasticsearchService service;

    public FileTransformer(GUOKIKClient client, ElasticsearchService service) {
        this.client = client;
        this.service = service;
    }

    @Override
    public Message<List<UpdateListTypeExt>> transform(Message<Territorial> source) {
        List<Regions> regionsList = source.getPayload().getRegionsList();

        Map<String, AddressVersionEntity> addressVersionsId = service.getAllTerytVersion();
        boolean isNotEmpty = !addressVersionsId.isEmpty();

        List<UpdateListTypeExt> list = regionsList.stream()
                .map(Regions::getTerytUnit)
                .map(Region::getRegionIdTeryt)
                .map(idTeryt -> {
                    UpdateListType updateList;
                    if (isNotEmpty && addressVersionsId.containsKey(idTeryt)) {
                        String verId = addressVersionsId.get(idTeryt).getVerId();
                        updateList = client.getPrzyrost(verId).getUpdateList();
                    } else {
                        updateList = client.getPelne(idTeryt).getUpdateList();
                    }
                    return new UpdateListTypeExt(updateList, idTeryt);
                })
                .collect(Collectors.toList());

        return MessageBuilder.withPayload(list.subList(5,6)).build();
    }

}
