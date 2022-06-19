package com.pgoogol.teryt.integration;

import com.pgoogol.teryt.integration.client.GUOKIKFeign;
import com.pgoogol.teryt.integration.model.teryt.Territorial;
import com.pgoogol.teryt.integration.service.TerytArchiveHandler;
import com.pgoogol.teryt.integration.transformer.ParseFileAndSaveTransformer;
import com.pgoogol.teryt.integration.transformer.UnZipFileTransformer;
import com.pgoogol.teryt.integration.model.teryt.UpdateListTypeExt;
import com.pgoogol.teryt.integration.transformer.DownloadFileTransformer;
import com.pgoogol.teryt.integration.transformer.FileTransformer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.handler.LoggingHandler;

@Configuration
public class TerytIntegrationConfig {

    private final GUOKIKFeign guokikFeign;
    private final FileTransformer fileTransformer;
    private final DownloadFileTransformer downloadFileTransformer;
    private final UnZipFileTransformer unZipFileTransformer;
    private final TerytArchiveHandler terytArchiveHandler;
    private final ParseFileAndSaveTransformer parseFileAndSaveTransformer;

    public TerytIntegrationConfig(GUOKIKFeign guokikFeign,
                                  FileTransformer fileTransformer,
                                  DownloadFileTransformer downloadFileTransformer,
                                  UnZipFileTransformer unZipFileTransformer,
                                  TerytArchiveHandler terytArchiveHandler,
                                  ParseFileAndSaveTransformer parseFileAndSaveTransformer) {
        this.guokikFeign = guokikFeign;
        this.fileTransformer = fileTransformer;
        this.downloadFileTransformer = downloadFileTransformer;
        this.unZipFileTransformer = unZipFileTransformer;
        this.terytArchiveHandler = terytArchiveHandler;
        this.parseFileAndSaveTransformer = parseFileAndSaveTransformer;
    }

    @Bean
    public IntegrationFlow terytFilePollingFlow1(@Value("${teryt.file.polling.cron.expression}") String pollExpression) {
        return IntegrationFlows.fromSupplier(this::terytFilePollingFlow, c -> c.poller(Pollers.cron(pollExpression)))
                .log(LoggingHandler.Level.DEBUG, new LiteralExpression("START PROCESS"))
                .transform(fileTransformer)
                .transform(downloadFileTransformer)
                .log(LoggingHandler.Level.DEBUG, new LiteralExpression("AFTER DOWNLOAD FILES"))
                .transform(unZipFileTransformer)
                .log(LoggingHandler.Level.DEBUG, new LiteralExpression("AFTER UNZIP FILES"))
                .split()
                .log(LoggingHandler.Level.DEBUG, m -> "PROCESS ADDRESS - TERYT ID -> " + ((UpdateListTypeExt)m.getPayload()).getTerytId())
                .transform(parseFileAndSaveTransformer)
                .aggregate()
                .log(LoggingHandler.Level.DEBUG, new LiteralExpression("AGGERGATE"))
                .log(LoggingHandler.Level.DEBUG, new LiteralExpression("END PROCESS"))
                .handle(terytArchiveHandler)
                .get();
    }

    @Bean
    public Territorial terytFilePollingFlow() {
        return guokikFeign.getWoj();
    }

}
