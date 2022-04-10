package com.pgoogol.teryt.integration;

import com.pgoogol.teryt.integration.model.teryt.Territorial;
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
                .log(LoggingHandler.Level.DEBUG, new LiteralExpression("start process"))
                .transform(fileTransformer)
                .transform(downloadFileTransformer)
                .log(LoggingHandler.Level.DEBUG, new LiteralExpression("after download files"))
                .transform(unZipFileTransformer)
                .log(LoggingHandler.Level.DEBUG, new LiteralExpression("after unzip files"))
                .split()
                .log(LoggingHandler.Level.DEBUG, m -> "process address - teryt id -> " + ((UpdateListTypeExt)m.getPayload()).getTerytId())
                .transform(parseFileAndSaveTransformer)
                .aggregate()
                .log(LoggingHandler.Level.DEBUG, new LiteralExpression("aggregate"))
                .log(LoggingHandler.Level.DEBUG, new LiteralExpression("end process"))
                .handle(terytArchiveHandler)
                .get();
    }

    @Bean
    public Territorial terytFilePollingFlow() {
        return guokikFeign.getWoj();
    }

}
