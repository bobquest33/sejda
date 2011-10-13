/*
 * Created on Oct 11, 2011
 * Copyright 2010 by Eduard Weissmann (edi.weissmann@gmail.com).
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.sejda.cli;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.sejda.core.exception.SejdaRuntimeException;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * 
 * Test verifying that short names are not repeated for each task cli interface
 * 
 * @author Eduard Weissmann
 * 
 */
public class OptionDecriptionAndShortNameTest extends AcrossAllTasksTraitTest {

    public OptionDecriptionAndShortNameTest(TestableTask testableTask) {
        super(testableTask);
    }

    @Test
    public void descriptionsHaveOptionalityInformation() {

        for (MethodAndOption eachMethod : extractOptionAnnotations()) {
            String description = eachMethod.getOption().description();
            boolean hasOptionalityInfo = StringUtils.endsWith(description, "(optional)")
                    || StringUtils.endsWith(description, "(required)");
            if (!hasOptionalityInfo) {
                throw new SejdaRuntimeException(getTaskName()
                        + " is missing optionality information [(optional) or (required)] on "
                        + eachMethod.getMethodName());
            }
        }
    }

    @Test
    public void shortNamesAreMandatory() {

        for (MethodAndOption eachMethod : extractOptionAnnotations()) {
            if (eachMethod.getNonBlankShortNames().isEmpty() && eachMethod.isNotBooleanFlag()) {
                throw new SejdaRuntimeException(getTaskName() + " has missing short name on "
                        + eachMethod.getMethodName());
            }
        }
    }

    @Test
    public void shortNamesAreUnique() {
        Map<String, Method> shortNamesMapping = new HashMap<String, Method>();

        for (MethodAndOption eachMethod : extractOptionAnnotations()) {

            for (String eachShortName : eachMethod.getNonBlankShortNames()) {
                if (shortNamesMapping.containsKey(eachShortName)) {
                    throw new SejdaRuntimeException(getTaskName() + " has duplicate short names: '" + eachShortName
                            + "' defined on " + eachMethod.getMethodName() + "() and on "
                            + shortNamesMapping.get(eachShortName).getName() + "()");
                }
                shortNamesMapping.put(eachShortName, eachMethod.getMethod());
            }
        }
    }

    private Collection<MethodAndOption> extractOptionAnnotations() {
        Collection<MethodAndOption> result = new ArrayList<OptionDecriptionAndShortNameTest.MethodAndOption>();

        Class<?> cliCommandClass = testableTask.getCorrespondingCliCommand().getCliArgumentsClass();

        for (Method eachMethod : cliCommandClass.getMethods()) {
            final Option optionAnnotation = eachMethod.getAnnotation(Option.class);

            if (optionAnnotation == null) {
                continue;
            }

            result.add(new MethodAndOption(eachMethod, optionAnnotation));
        }

        return result;
    }

    class MethodAndOption {
        private final Method method;
        private final Option option;

        /**
         * @param method
         * @param option
         */
        MethodAndOption(Method method, Option option) {
            super();
            this.method = method;
            this.option = option;
        }

        /**
         * @return true if the Option defined by the method is not a flag that has only true/false values (these are known to always be optional options)
         */
        public boolean isNotBooleanFlag() {
            return !method.getReturnType().equals(boolean.class);
        }

        /**
         * @return name of the method
         */
        public String getMethodName() {
            return getMethod().getName();
        }

        public Collection<String> getNonBlankShortNames() {
            Collection<String> result = new ArrayList<String>(Arrays.asList(getOption().shortName()));
            result.remove("");
            return result;
        }

        Method getMethod() {
            return method;
        }

        Option getOption() {
            return option;
        }
    }
}
