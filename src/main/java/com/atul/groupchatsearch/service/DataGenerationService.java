package com.atul.groupchatsearch.service;

import com.atul.groupchatsearch.entity.ChatMessage;
import com.atul.groupchatsearch.repository.ChatMessageRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DataGenerationService {

    private final ChatMessageRepository chatMessageRepository;
    private final EmbeddingModel embeddingModel;
    private final Random random = new Random();

    private final String[] USERS = {"Priya", "Rahul", "Amit", "Neha", "Rohan", "Anjali", "Vikash", "Sneha"};

    private final String[] PHRASES = {
            "bhai kya chal raha hai", "kaha ho sab", "class hai kya aaj?", "proxy laga dena plz",
            "canteen aaja jaldi", "assignment submit kar diya?", "kal quiz hai wtf", "sir aaye hai kya class me?",
            "bhai notes bhej de", "mess ka khana kaisa hai aaj?", "kisi pe previous year paper hai?",
            "attendance kitni hui teri?", "bhai padhai shuru karni padegi", "mujhe kuch samajh nahi aa raha",
            "exam kab se start hai?", "result aa gaya kya?", "link forward kar de", "meet join kar lo sab", "aaj lab hogi kya?"
    };

    public DataGenerationService(ChatMessageRepository chatMessageRepository, EmbeddingModel embeddingModel) {
        this.chatMessageRepository = chatMessageRepository;
        this.embeddingModel = embeddingModel;
    }

    @Transactional
    public void generateAndIngestData() {
        chatMessageRepository.deleteAll();

        List<ChatMessage> messages = new ArrayList<>();
        LocalDateTime startDate = LocalDateTime.now().minusMonths(6);

        // Generate Random Filler Messages
        for (int i = 0; i < 495; i++) {
            String sender = USERS[random.nextInt(USERS.length)];
            LocalDateTime timestamp = startDate.plusHours(random.nextInt(4320)); // Random hour within 6 months

            // Construct a random Hinglish sentence
            String content = buildRandomSentence();

            messages.add(new ChatMessage(sender, timestamp, content, null));
        }

        // Insert "Needle in the Haystack" Messages
        messages.add(new ChatMessage(
                "Priya",
                LocalDateTime.now().minusDays(15),
                "chalo Manali fix hai, budget 10k per head rakhte hai, december end me nikalte hai",
                null
        ));
        messages.add(new ChatMessage(
                "Rahul",
                LocalDateTime.now().minusDays(30),
                "Bhai minor project final ho gaya? OFDM communication system in Verilog banayenge, Xilinx FPGA pe test karenge.",
                null
        ));
        messages.add(new ChatMessage(
                "Neha",
                LocalDateTime.now().minusDays(45),
                "Smart India Hackathon ki last date aa gayi hai, registration kar diya kya?",
                null
        ));
        messages.add(new ChatMessage(
                "Amit",
                LocalDateTime.now().minusDays(60),
                "Goldman Sachs ka assessment kaisa gaya? Array aur backtracking ke questions hard the thode.",
                null
        ));
        messages.add(new ChatMessage(
                "Sneha",
                LocalDateTime.now().minusDays(10),
                "Kal subah 9 baje lab me milte hai, circuit test karna hai.",
                null
        ));

        // Sort messages chronologically to mimic a real chat history
        messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));

        // Generate embeddings and save in batches to avoid overwhelming memory/API
        System.out.println("Starting vector embedding generation for 500 messages...");

        List<ChatMessage> batch = new ArrayList<>();
        int batchSize = 100;

        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);

            // Call Spring AI Embedding API
            float[] vector = embeddingModel.embed(msg.getContent());
            msg.setEmbedding(vector);

            batch.add(msg);

            if (batch.size() == batchSize || i == messages.size() - 1) {
                chatMessageRepository.saveAll(batch);
                batch.clear();
                System.out.println("Processed and saved " + (i + 1) + " messages.");
            }
        }
        System.out.println("Data generation and ingestion complete.");
    }

    private String buildRandomSentence() {
        int length = random.nextInt(3) + 1;
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < length; j++) {
            sb.append(PHRASES[random.nextInt(PHRASES.length)]).append(" ");
        }
        return sb.toString().trim();
    }
}