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

package ru.tinkoff.eclair.aop;

import org.aopalliance.intercept.MethodInvocation;
import ru.tinkoff.eclair.definition.method.MethodLog;
import ru.tinkoff.eclair.logger.EclairLogger;

import java.util.List;

/**
 * @author Vyacheslav Klapatnyuk
 */
final class LogAdvisor extends AbstractAdvisor<MethodLog> {

    private final EclairLogger eclairLogger;

    private LogAdvisor(List<MethodLog> methodLogs,
                       EclairLogger eclairLogger) {
        super(methodLogs);
        this.eclairLogger = eclairLogger;
    }

    static LogAdvisor newInstance(EclairLogger eclairLogger, List<MethodLog> methodLogs) {
        return methodLogs.isEmpty() ? null : new LogAdvisor(methodLogs, eclairLogger);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        MethodLog methodLog = methodDefinitions.get(invocation.getMethod());
        eclairLogger.logInIfNecessary(invocation, methodLog);
        Object result;
        try {
            result = invocation.proceed();
        } catch (Throwable throwable) {
            eclairLogger.logErrorIfNecessary(invocation, methodLog, throwable);
            throw throwable;
        }
        eclairLogger.logOutIfNecessary(invocation, methodLog, result);
        return result;
    }

    EclairLogger getEclairLogger() {
        return eclairLogger;
    }
}
