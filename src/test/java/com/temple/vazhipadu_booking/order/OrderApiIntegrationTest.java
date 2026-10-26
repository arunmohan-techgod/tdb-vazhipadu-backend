package com.temple.vazhipadu_booking.order;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long createOffering(String name, int price, boolean active)
            throws Exception {

        String response = mockMvc.perform(post("/api/offerings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "%s",
                      "price": %d,
                      "active": %s
                    }
                    """.formatted(name, price, active)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void shouldCalculateOrderTotalCorrectly() throws Exception {
        long pushpanjali =
            createOffering("Pushpanjali", 30, true);
        long archana =
            createOffering("Archana", 50, true);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "devoteeName": "Arun",
                      "phoneNumber": "9876543210",
                      "nakshatra": "Ashwathi",
                      "offeringDate": "2026-11-01",
                      "items": [
                        {
                          "offeringId": %d,
                          "quantity": 2
                        },
                        {
                          "offeringId": %d,
                          "quantity": 1
                        }
                      ]
                    }
                    """.formatted(pushpanjali, archana)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.total").value(110.0))
            .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void shouldRejectInactiveOffering() throws Exception {
        long inactiveId =
            createOffering("Inactive Offering", 30, false);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "devoteeName": "Arun",
                      "phoneNumber": "9876543210",
                      "nakshatra": "Ashwathi",
                      "offeringDate": "2026-11-01",
                      "items": [
                        {
                          "offeringId": %d,
                          "quantity": 1
                        }
                      ]
                    }
                    """.formatted(inactiveId)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                .value("Offering is inactive: Inactive Offering"))
            .andExpect(jsonPath("$.path").value("/api/orders"));
    }

    @Test
    void shouldReturnConsistentErrorWhenOrderDoesNotExist()
            throws Exception {
        mockMvc.perform(get("/api/orders/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message")
                .value("Order not found: 999999"))
            .andExpect(jsonPath("$.path")
                .value("/api/orders/999999"));
    }
}
