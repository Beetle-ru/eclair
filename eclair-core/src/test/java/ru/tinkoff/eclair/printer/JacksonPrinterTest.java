/*
 * Copyright 2018 Tinkoff Bank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.eclair.printer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.annotation.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class JacksonPrinterTest {

    private XmlMapper xmlMapper;

    @Before
    public void init() {
        xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JaxbAnnotationModule());
    }

    @Test
    public void serialize() {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonPrinter jacksonPrinter = new JacksonPrinter(objectMapper);
        Empty empty = new Empty();
        empty.setValue("value");
        // when
        String json = jacksonPrinter.serialize(empty);
        // then
        assertThat(json, is("{\"value\":\"value\"}"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void serializeException() throws JsonProcessingException {
        // given
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonGenerationException("", (JsonGenerator) null));
        JacksonPrinter jacksonPrinter = new JacksonPrinter(objectMapper);
        String string = "string";
        // when
        jacksonPrinter.serialize(string);
        // then expected exception
    }

    @Test
    public void serializeXmlRoot() {
        // given
        JacksonPrinter jacksonPrinter = new JacksonPrinter(xmlMapper);
        Root input = new Root();
        input.setValue("value");
        // when
        String xml = jacksonPrinter.serialize(input);
        // then
        assertThat(xml, is("<someName><value>value</value></someName>"));
    }

    @Test
    public void serializeXmlNotRoot() {
        // given
        JacksonPrinter jacksonPrinter = new JacksonPrinter(xmlMapper);
        NotRoot input = new NotRoot();
        input.setValue("value");
        // when
        String xml = jacksonPrinter.serialize(input);
        // then
        assertThat(xml, is("<NotRoot><value>value</value></NotRoot>"));
    }

    @Test
    public void serializeXmlEmpty() {
        // given
        JacksonPrinter jacksonPrinter = new JacksonPrinter(xmlMapper);
        Empty input = new Empty();
        input.setValue("value");
        // when
        String xml = jacksonPrinter.serialize(input);
        // then
        assertThat(xml, is("<Empty><value>value</value></Empty>"));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = "value")
    @XmlRootElement(name = "someName")
    public static class Root {

        @XmlElement
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = "value")
    public static class NotRoot {

        @XmlElement
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Empty {

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
