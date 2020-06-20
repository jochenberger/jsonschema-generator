/*
 * Copyright 2020 VicTools.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.victools.jsonschema.module.swagger2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.swagger2.IntegrationTest.IReference;
import com.github.victools.jsonschema.module.swagger2.IntegrationTest.Person;
import com.github.victools.jsonschema.module.swagger2.IntegrationTest.PersonReference;

import io.swagger.v3.oas.annotations.media.Schema;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.runner.RunWith;

/**
 * Integration test of this module being used in a real SchemaGenerator instance.
 */
@RunWith(JUnitParamsRunner.class)
public class IntegrationTest {

    public Object[] parametersForTestIntegration() {
        return new Object[][]{
            {TestClass.class},
            {Foo.class}
        };
    }

    @Test
    @Parameters
    public void testIntegration(Class<?> rawTargetType) throws Exception {
        Swagger2Module module = new Swagger2Module();
        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON)
                .with(module);
        SchemaGenerator generator = new SchemaGenerator(configBuilder.build());
        JsonNode result = generator.generateSchema(rawTargetType);

        String rawJsonSchema = result.toString();
        JSONAssert.assertEquals('\n' + rawJsonSchema + '\n',
                loadResource("integration-test-result-" + rawTargetType.getSimpleName() + ".json"), rawJsonSchema,
                JSONCompareMode.STRICT);
    }

    private static String loadResource(String resourcePath) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = IntegrationTest.class.getResourceAsStream(resourcePath);
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            while (scanner.hasNext()) {
                stringBuilder.append(scanner.nextLine()).append('\n');
            }
        }
        String fileAsString = stringBuilder.toString();
        return fileAsString;

    }

    @Schema(title = "test title", description = "test description")
    static class TestClass {

        @Schema(hidden = true)
        public Object hiddenField;

        @Schema(name = "fieldWithOverriddenName")
        public List<Boolean> originalFieldName;

        @Schema(description = "field description", nullable = true, allowableValues = {"A", "B", "C", "D"})
        public String fieldWithDescriptionAndAllowableValues;

        @Schema(minimum = "15", maximum = "20")
        public int fieldWithInclusiveNumericRange;

        @Schema(minimum = "14", maximum = "21", exclusiveMinimum = true, exclusiveMaximum = true)
        public int fieldWithExclusiveNumericRange;
    }

    interface IReference {

        String getName();
    }

    class Reference<T> implements IReference {

        private String name;

        @Override
        public String getName() {
            return this.name;
        }
    }

    class Person {

    }

    @Schema(description = "the foo's person")
    class PersonReference extends Reference<Person> {

        @Override
        @Schema(description = "the person's name")
        public String getName() {
            return super.getName();
        }
    }

    class Foo {

        @Schema(implementation = PersonReference.class)
        private Reference<Person> person;
    }
}
