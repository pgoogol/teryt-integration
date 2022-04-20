package com.pgoogol.teryt.integration.transformer;

import com.pgoogol.teryt.integration.model.teryt.AddressFiles;
import com.pgoogol.teryt.integration.model.teryt.UpdateListTypeExt;
import com.pgoogol.teryt.integration.service.PollingDirectoryManager;
import com.pgoogol.teryt.integration.wsdl.offline.Dane;
import com.pgoogol.teryt.integration.wsdl.offline.UpdateType;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

@Component
public class DownloadFileTransformer implements GenericTransformer<Message<List<UpdateListTypeExt>>, Message<List<AddressFiles>>> {

    private final Logger log = LogManager.getLogger(DownloadFileTransformer.class);

    private final PollingDirectoryManager pollingDirectoryManager;

    public DownloadFileTransformer(PollingDirectoryManager pollingDirectoryManager) {
        this.pollingDirectoryManager = pollingDirectoryManager;
    }

    @Override
    public Message<List<AddressFiles>> transform(Message<List<UpdateListTypeExt>> source) {
        List<AddressFiles> filesList = new LinkedList<>();
        source.getPayload().forEach(updateListType -> {
            log.info(String.format("start download version %s", updateListType.getVerId()));
            AddressFiles files = new AddressFiles();
            files.setVerId(updateListType.getVerId());
            files.setTerytId(updateListType.getTerytId());
            updateListType.getUpdate()
                    .stream()
                    .filter(updateType -> Dane.ADR.equals(updateType.getSln()))
                    .map(UpdateType::getUrl)
                    .forEach(s -> {
                        try {
                            URL url = new URL(s);
                            String[] split = s.split("/");
                            File destination = new File(
                            pollingDirectoryManager.getPollingDir() + File.separator + split[split.length - 1]
                            );
                            //TODO verify what is doing
                            FileUtils.copyURLToFile(url, destination);
                            files.addZipFile(destination);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            filesList.add(files);
            log.info(String.format("end download version %s", updateListType.getVerId()));
        });
        return MessageBuilder.withPayload(filesList).build();
    }

}
