package org.example.shopdemo.agent;

import org.example.shopdemo.dto.AIRecommendRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnhancedAIService {

    @Autowired
    private ShoppingAgent shoppingAgent;

    public String intelligentCustomerService(Long userId, String question) {
        return shoppingAgent.handleCustomerService(userId, question);
    }

    public String personalizedRecommendation(Long userId, AIRecommendRequest request) {
        return shoppingAgent.handleProductRecommendation(userId, request);
    }

    public String intelligentSearch(String searchQuery) {
        return shoppingAgent.handleProductSearch(searchQuery);
    }

    public String intelligentOrderAssistant(Long userId, String orderNo) {
        return shoppingAgent.handleOrderAssistant(userId, orderNo);
    }

    public String intelligentProductComparison(Long userId, Long product1Id, Long product2Id) {
        return shoppingAgent.handleProductComparison(userId, product1Id, product2Id);
    }
}
