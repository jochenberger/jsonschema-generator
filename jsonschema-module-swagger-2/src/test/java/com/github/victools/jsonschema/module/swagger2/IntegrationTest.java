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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Integration test of this module being used in a real SchemaGenerator
 * instance.
 */
public class IntegrationTest {

    /**
     * Test
     *
     * @throws Exception
     */
    @Test
    public void testIntegration() throws Exception {
        // active all optional modules
        Swagger2Module module = new Swagger2Module();
        SchemaGeneratorConfig config = new SchemaGeneratorConfigBuilder(new ObjectMapper(),
                SchemaVersion.DRAFT_2019_09).with(module).build();
        SchemaGenerator generator = new SchemaGenerator(config);
        JsonNode result = generator.generateSchema(TestClass.class);

        String rawJsonSchema = result.toString();
        System.out.println(rawJsonSchema);
        JSONAssert.assertEquals('\n' + rawJsonSchema + '\n',
                loadResource("integration-test-result.json"), rawJsonSchema,
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

        @Schema(description = "field description", nullable = true, allowableValues = { "A", "B",
                "C", "D" })
        public String fieldWithDescriptionAndAllowableValues;

        @Schema(minimum = "15", maximum = "20")
        public int fieldWithInclusiveNumericRange;

        @Schema(minimum = "14", maximum = "21", exclusiveMinimum = true, exclusiveMaximum = true)
        public int fieldWithExclusiveNumericRange;
    }
}
