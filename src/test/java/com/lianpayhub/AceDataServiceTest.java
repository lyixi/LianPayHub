package com.lianpayhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lianpayhub.common.error.BusinessException;
import com.lianpayhub.domain.ai.AiProviderConfig;
import com.lianpayhub.service.ai.AceDataService;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class AceDataServiceTest {
    private AceDataService service;
    private MockRestServiceServer upstream;
    private AiProviderConfig config;

    @BeforeEach void setUp() throws Exception {
        service = new AceDataService(new ObjectMapper());
        upstream = MockRestServiceServer.bindTo((RestTemplate) ReflectionTestUtils.getField(service, "client")).build();
        config = new AiProviderConfig("acedata", "ACE Data", "https://untrusted.example", null, "{}", "{\"apiKey\":\"test-platform-key\"}");
    }

    @Test void catalogCoversTextImagesVideoAudioAndTasksWithUniqueIds() {
        Set<Object> ids = new HashSet<>();
        Set<Object> categories = new HashSet<>();
        for (Map<String, Object> operation : service.operations()) {
            assertTrue(ids.add(operation.get("id")), "Duplicate operation");
            assertTrue(((String) operation.get("path")).startsWith("/"));
            assertFalse(((String) operation.get("path")).contains(".."));
            categories.add(operation.get("category"));
        }
        assertTrue(ids.containsAll(Arrays.asList("minimax-videos", "minimax-tasks", "openai-chat-completions", "flux-images", "suno-audios")));
        assertEquals(5, categories.size());
    }

    @Test void postsToFixedOriginWithPlatformKeyAndKeepsTaskResponse() {
        upstream.expect(requestTo("https://api.acedata.cloud/minimax/videos"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("Authorization", "Bearer test-platform-key"))
                .andExpect(content().json("{\"async\":true,\"prompt\":\"test\"}"))
                .andRespond(withSuccess("{\"task_id\":\"task-123\",\"trace_id\":\"trace-456\"}", MediaType.APPLICATION_JSON));
        Map<String, Object> body = new LinkedHashMap<>(); body.put("async", true); body.put("prompt", "test");
        Map<?, ?> response = (Map<?, ?>) service.invoke(config, "minimax-videos", body, null);
        assertEquals("task-123", response.get("task_id")); upstream.verify();
    }

    @Test void usesUserCredentialForApplicationCalls() {
        upstream.expect(requestTo("https://api.acedata.cloud/minimax/tasks"))
                .andExpect(header("Authorization", "Bearer user-key"))
                .andRespond(withSuccess("{\"status\":\"completed\"}", MediaType.APPLICATION_JSON));
        service.invoke(config, "minimax-tasks", Collections.singletonMap("id", "task-123"), "user-key");
        upstream.verify();
    }

    @Test void encodesGetQueryParametersOnce() {
        upstream.expect(requestTo("https://api.acedata.cloud/fish/model?title=hello%20world"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"items\":[]}", MediaType.APPLICATION_JSON));
        service.invoke(config, "fish-model-query", Collections.singletonMap("title", "hello world"), null);
        upstream.verify();
    }

    @Test void returnsBinarySpeechWithoutCorruptingIt() {
        byte[] audio = new byte[] {0, 1, 2, -1, 42};
        upstream.expect(requestTo("https://api.acedata.cloud/v1/audio/speech"))
                .andRespond(withSuccess(audio, MediaType.valueOf("audio/mpeg")));
        Map<?, ?> response = (Map<?, ?>) service.invoke(config, "openai-audio-speech", Collections.singletonMap("input", "hello"), null);
        assertEquals("audio/mpeg", response.get("contentType"));
        assertArrayEquals(audio, Base64.getDecoder().decode((String) response.get("base64"))); upstream.verify();
    }

    @Test void rejectsUnknownOperationsDisabledProvidersAndStreamingBeforeNetwork() {
        assertThrows(BusinessException.class, () -> service.invoke(config, "https://attacker.example", null, null));
        assertThrows(BusinessException.class, () -> service.invoke(config, "openai-chat-completions", Collections.singletonMap("stream", true), null));
        config.changeEnabled(false);
        assertThrows(BusinessException.class, () -> service.invoke(config, "minimax-videos", null, null));
        upstream.verify();
    }

    @Test void rejectsMissingKeyAndWrongProvider() {
        AiProviderConfig empty = new AiProviderConfig("acedata", "ACE Data", null, null, "{}", "{}");
        assertThrows(BusinessException.class, () -> service.invoke(empty, "minimax-videos", null, null));
        AiProviderConfig wrong = new AiProviderConfig("deepseek", "DeepSeek", null, null, "{}", "{}");
        assertThrows(BusinessException.class, () -> service.invoke(wrong, "minimax-videos", null, null));
    }

    @Test void doesNotExposeSecretsFromUpstreamErrors() {
        upstream.expect(requestTo("https://api.acedata.cloud/minimax/videos"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("Authorization: Bearer test-platform-key"));
        BusinessException error = assertThrows(BusinessException.class, () -> service.invoke(config, "minimax-videos", null, null));
        assertTrue(error.getMessage().contains("401"));
        assertFalse(error.getMessage().contains("test-platform-key")); upstream.verify();
    }
}
