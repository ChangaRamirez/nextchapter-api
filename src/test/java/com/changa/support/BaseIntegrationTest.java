package com.changa.support;

import com.changa.auth.dto.RegisterRequestDto;
import com.changa.book.domain.dto.CreateBookRequestDto;
import com.changa.reading.domain.dto.CreateReadingEntryRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JsonMapper jsonMapper;

    protected String registerAndGetToken(RegisterRequestDto request) throws Exception {

        //Registration returns a JWT token.
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andReturn();

        String jsonContent = result.getResponse().getContentAsString();

        JsonNode node = jsonMapper.readTree(jsonContent);

        return node.get("token").asText(); //This is the beautiful token extracted
    }

    protected UUID createBookAndReturnId(String token, CreateBookRequestDto request) throws Exception {

        MvcResult result = mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andReturn();

        String jsonContent = result.getResponse().getContentAsString();

        JsonNode node = jsonMapper.readTree(jsonContent);

        return UUID.fromString(node.get("id").asText());
    }

    protected UUID createReadingEntryAndReturnId(String token, CreateReadingEntryRequestDto request) throws Exception {

        MvcResult createReadingEntryResult = mockMvc.perform(post("/api/v1/reading-entries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()).andReturn();

        String createReadingEntryJsonContent = createReadingEntryResult.getResponse().getContentAsString();

        JsonNode createReadingEntryNode = jsonMapper.readTree(createReadingEntryJsonContent);

        return UUID.fromString(createReadingEntryNode.get("id").asText());
    }
}
