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

package ru.tinkoff.eclair.validate.log.single;

import org.springframework.boot.logging.LogLevel;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.core.PrinterResolver;
import ru.tinkoff.eclair.printer.Printer;

import java.lang.annotation.Annotation;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.springframework.boot.logging.LogLevel.OFF;

/**
 * TODO: verbose, printer for empty parameter array (or (v|V)oid return type)
 * TODO: explicit 'OFF' level
 *
 * @author Vyacheslav Klapatnyuk
 */
abstract class LogAnnotationValidator implements Validator {

    private final PrinterResolver printerResolver;

    LogAnnotationValidator(PrinterResolver printerResolver) {
        this.printerResolver = printerResolver;
    }

    @Override
    public void validate(Object target, Errors errors) {
        Annotation annotation = (Annotation) target;

        LogLevel expectedLevel = AnnotationAttribute.LEVEL.extract(annotation);
        LogLevel ifEnabledLevel = AnnotationAttribute.IF_ENABLED.extract(annotation);
        if (ifEnabledLevel.ordinal() >= expectedLevel.ordinal() && ifEnabledLevel != OFF) {
            errors.reject("ifEnabled",
                    format("'If enabled' level is higher or equals to expected level: %s", annotation));
        }

        String printerName = AnnotationAttribute.PRINTER.extract(annotation);
        if (StringUtils.hasText(printerName)) {
            Map<String, Printer> printers = printerResolver.getPrinters();
            Printer printer = printers.get(printerName);
            if (isNull(printer)) {
                printer = printers.get(printerResolver.getAliases().get(printerName));
                if (isNull(printer)) {
                    errors.reject("printer.unknown",
                            format("Unknown printer '%s' specified for annotation: %s", printerName, annotation));
                }
            }
        }
    }
}