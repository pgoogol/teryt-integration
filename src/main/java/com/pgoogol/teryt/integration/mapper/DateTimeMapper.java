package com.pgoogol.teryt.integration.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Optional;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.FIELD)
public interface DateTimeMapper {

    default XMLGregorianCalendar map(LocalDateTime dateTime) {
        return Optional.ofNullable(dateTime)
                .map(localDateTime -> ZonedDateTime.of(localDateTime, ZoneId.systemDefault()))
                .map(GregorianCalendar::from)
                .map(gregorianCalendar -> {
                    try {
                        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                    } catch (DatatypeConfigurationException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .orElse(null);
    }

    default LocalDateTime map(XMLGregorianCalendar dateTime) {
        return Optional.ofNullable(dateTime)
                .map(XMLGregorianCalendar::toGregorianCalendar)
                .map(GregorianCalendar::toZonedDateTime)
                .map(ZonedDateTime::toLocalDateTime)
                .orElse(null);
    }
}
