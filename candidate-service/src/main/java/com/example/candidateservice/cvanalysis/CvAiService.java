package com.example.candidateservice.cvanalysis;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CvAiService {

    private final ChatClient chatClient;
    private final String modelName;

    // Le Builder est automatiquement configuré par le starter Spring AI Ollama
    public CvAiService(
            ChatClient.Builder chatClientBuilder,
            @Value("${spring.ai.ollama.chat.model:${spring.ai.ollama.chat.options.model:unknown}}") String modelName
    ) {
        this.chatClient = chatClientBuilder.build();
        this.modelName = modelName;
    }

    public String generateAnalysisReport(String cvText) {
        String prompt = """
            Tu es un expert en recrutement RH. Analyse le texte du CV suivant :
            %s
            
            Rédige un rapport clair contenant :
            1. Un résumé des compétences clés et technologies maîtrisées.
            2. Une évaluation globale du profil.
            """.formatted(cvText);

        return callOllama("analysis-report", prompt);
    }

    public String generateInterviewQuestions(String cvText) {
        String prompt = """
            En te basant uniquement sur ce CV :
            %s
            
            Génère exactement 5 questions d'entretien d'embauche personnalisées, techniques et stimulantes.
            """.formatted(cvText);

        return callOllama("interview-questions", prompt);
    }

    private String callOllama(String operation, String prompt) {
        long startedAt = System.nanoTime();
        log.info("Ollama call started: operation={}, model={}", operation, modelName);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            int responseLength = response == null ? 0 : response.length();
            log.info("Ollama call completed: operation={}, model={}, durationMs={}, responseChars={}",
                    operation, modelName, durationMs, responseLength);
            return response;
        } catch (RuntimeException exception) {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.error("Ollama call failed: operation={}, model={}, durationMs={}",
                    operation, modelName, durationMs, exception);
            throw exception;
        }
    }
}
