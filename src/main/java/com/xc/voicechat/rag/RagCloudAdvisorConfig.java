//package com.xc.voicechat.rag;
//
//import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
//import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
//import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
//import com.xc.voicechat.config.VoiceChatConfig;
//import jakarta.annotation.Resource;
//import org.springframework.ai.chat.client.advisor.api.Advisor;
//import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
//import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RagCloudAdvisorConfig {
//
//    @Resource
//    private VoiceChatConfig config;
//
//    @Bean
//    public Advisor ragCloudAdvisor() {
//        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(config.getApiKey()).build();
//        //云知识库名称
//        final String knowledgeIndex = config.getKnowledge() == null ? "default" : config.getKnowledge();
//        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi, DashScopeDocumentRetrieverOptions
//                .builder()
//                .withIndexName(knowledgeIndex)
//                .withDenseSimilarityTopK(10)
//                .build());
//        return RetrievalAugmentationAdvisor.builder()
//                .documentRetriever(documentRetriever)
//                .build();
//    }
//}
