package com.pgoogol.teryt.integration.service;

import com.pgoogol.teryt.integration.wsdl.offline.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import javax.xml.bind.JAXBElement;

public class GUOKIKClient extends WebServiceGatewaySupport {

    private final Logger log = LogManager.getLogger(GUOKIKClient.class);

    @SuppressWarnings("unchecked")
    public PobierzPelneResponse getPelne(String teryt) {

        ObjectFactory objectFactory = new ObjectFactory();
        PobierzPelne request = objectFactory.createPobierzPelne();
        request.setTeryt(teryt);

        log.info(String.format("Requesting location for teryt id = %s", teryt));
        JAXBElement<PobierzPelne> pobierzPelne = objectFactory.createPobierzPelne(request);
        JAXBElement<PobierzPelneResponse> element = (JAXBElement<PobierzPelneResponse>) getWebServiceTemplate().marshalSendAndReceive(pobierzPelne);
        return element.getValue();
    }

    @SuppressWarnings("unchecked")
    public PobierzPrzyrostResponse getPrzyrost(String version) {
        ObjectFactory objectFactory = new ObjectFactory();
        PobierzPrzyrost request = objectFactory.createPobierzPrzyrost();
        request.setVerId(version);

        log.info(String.format("Requesting location for version id = %s", version));
        JAXBElement<PobierzPrzyrost> pobierzPrzyrost = objectFactory.createPobierzPrzyrost(request);
        JAXBElement<PobierzPrzyrostResponse> element = (JAXBElement<PobierzPrzyrostResponse>) getWebServiceTemplate().marshalSendAndReceive(pobierzPrzyrost);
        return element.getValue();
    }

}
