package com.pgoogol.teryt.integration.config;

import com.pgoogol.teryt.integration.GUOKIKClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

@Configuration
public class SoupConfig {

    private static final String WSDL_OFFLINE_PATH = "com.pgoogol.teryt.integration.wsdl.offline";
    private static final String WSDL_ONLINE_PATH = "com.pgoogol.teryt.integration.wsdl.online";
    private static final String DICTIONARY_OFFLINE_URI = "http://mapy.geoportal.gov.pl/wss/service/slnoff/guest/slowniki-offline";

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
        client.setDefaultUri(DICTIONARY_OFFLINE_URI);
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }

}
