package lt.linas_puplauskas.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import lt.linas_puplauskas.model.dto.AIAnalysisResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class GroqService {
    private final OpenAIClient client;

    public GroqService(@Value("${groq.api.key}") String apiKey) {
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl("https://api.groq.com/openai/v1")
                .build();
    }

    public AIAnalysisResult analyze(String userInput) {
        String prompt = """
            You are a strict JSON generator.
            
            Convert the user message into a ticket JSON with this structure:
            
            {
              "shouldBeTicket": boolean,
              "title": string,
              "summary": string,
              "category": "BUG" | "FEATURE" | "OTHER",
              "priority": "LOW" | "MEDIUM" | "HIGH"
            }
            
            Rules:
            - NEVER return null values
            - Always fill all fields
            - If unsure, guess reasonable values
            - Output ONLY JSON
            
            User message:
            """ + userInput;

        ChatCompletion completion = client.chat().completions().create(
                ChatCompletionCreateParams.builder()
                        .model("llama-3.3-70b-versatile")
                        .addUserMessage(prompt)
                        .build()
        );

        String json = completion.choices().get(0).message().content().orElseThrow();

        System.out.println("--- RAW AI RESPONSE ---");
        System.out.println(json);

        return parseJson(json);
    }

    private AIAnalysisResult parseJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, AIAnalysisResult.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
}
