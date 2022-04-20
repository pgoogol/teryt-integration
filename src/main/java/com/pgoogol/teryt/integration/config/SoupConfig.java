package com.pgoogol.teryt.integration.config;

import com.pgoogol.teryt.integration.service.GUOKIKClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

@Configuration
public class SoupConfig {

    private static final String WSDL_OFFLINE_PATH = "com.pgoogol.teryt.integration.wsdl.offline";
    private static final String WSDL_ONLINE_PATH = "com.pgoogol.teryt.integration.wsdl.online";
    private final String dictionary_offline_uri;

    public SoupConfig(@Value("${teryt.guokik.uri}") String dictionary_offline_uri) {
        this.dictionary_offline_uri = dictionary_offline_uri;
    }

    @Bean
    public SaajSoapMessageFactory messageFactory() {
        SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
        messageFactory.setSoapVersion(SoapVersion.SOAP_12);
        return messageFactory;
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPaths(WSDL_OFFLINE_PATH, WSDL_ONLINE_PATH);
        return marshaller;
    }

    @Bean
    public GUOKIKClient guokikClient(Jaxb2Marshaller marshaller) {
        GUOKIKClient client = new GUOKIKClient();
        client.setDefaultUri(dictionary_offline_uri);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }

}
