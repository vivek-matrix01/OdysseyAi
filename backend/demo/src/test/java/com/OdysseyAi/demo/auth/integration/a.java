package com.OdysseyAi.demo.auth.integration;

import com.OdysseyAi.demo.auth.entity.User;
import com.OdysseyAi.demo.auth.interfaces.EmailService;
import com.OdysseyAi.demo.auth.repository.UserRepository;
import com.OdysseyAi.demo.common.enums.TripStatus;
import com.OdysseyAi.demo.trip.entity.Trip;
import com.OdysseyAi.demo.trip.repository.ActivityRepository;
import com.OdysseyAi.demo.trip.repository.ExpenseRepository;
import com.OdysseyAi.demo.trip.repository.ItineraryDayRepository;
import com.OdysseyAi.demo.trip.repository.ItineraryRepository;
import com.OdysseyAi.demo.trip.repository.TripMemberRepository;
import com.OdysseyAi.demo.trip.repository.TripRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class TripIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private TripMemberRepository tripMemberRepository;

    @Autowired
    private ItineraryRepository itineraryRepository;

    @Autowired
    private ItineraryDayRepository itineraryDayRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    /*
     * Prevent integration tests from sending real emails.
     */
    @MockBean
    private EmailService emailService;


    // =========================================================
    // 1. TRIP CRUD
    // =========================================================

    @Test
    void tripCrud_shouldWork() throws Exception {

        String token = createVerifiedUserAndLogin();

        String createTripRequest = """
                {
                    "title": "Delhi Trip",
                    "description": "Three day Delhi trip",
                    "destination": "Delhi",
                    "startDate": "2026-09-10",
                    "endDate": "2026-09-12"
                }
                """;

        String response =
                mockMvc.perform(
                                post("/api/trips")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(createTripRequest)
                        )
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.data.id").exists())
                        .andExpect(jsonPath("$.data.title")
                                .value("Delhi Trip"))
                        .andExpect(jsonPath("$.data.status")
                                .value("PLANNED"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Number tripIdNumber =
                JsonPath.read(response, "$.data.id");

        Long tripId = tripIdNumber.longValue();

        mockMvc.perform(
                        get("/api/trips/{id}", tripId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id")
                        .value(tripId))
                .andExpect(jsonPath("$.data.title")
                        .value("Delhi Trip"));

        String updateTripRequest = """
                {
                    "title": "Updated Delhi Trip",
                    "description": "Updated description",
                    "destination": "Delhi and Agra",
                    "startDate": "2026-09-10",
                    "endDate": "2026-09-12"
                }
                """;

        mockMvc.perform(
                        put("/api/trips/{id}", tripId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateTripRequest)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title")
                        .value("Updated Delhi Trip"))
                .andExpect(jsonPath("$.data.destination")
                        .value("Delhi and Agra"));
    }


    // =========================================================
    // 2. TRIP LIFECYCLE
    // =========================================================

    @Test
    void tripLifecycle_shouldFollowValidTransitions()
            throws Exception {

        String token = createVerifiedUserAndLogin();

        Long tripId =
                createTrip(token, "Lifecycle Trip");

        // PLANNED -> ONGOING
        mockMvc.perform(
                        post("/api/trips/{id}/start", tripId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());

        // ONGOING -> COMPLETED
        mockMvc.perform(
                        post("/api/trips/{id}/complete", tripId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());

        // COMPLETED -> CANCELLED should fail
        mockMvc.perform(
                        post("/api/trips/{id}/cancel", tripId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isBadRequest());

        Trip trip =
                tripRepository.findById(tripId)
                        .orElseThrow();

        assertEquals(
                TripStatus.COMPLETED,
                trip.getStatus()
        );
    }


    // =========================================================
    // 3. OWNERSHIP + SHARED MEMBER ACCESS
    // =========================================================

    @Test
    void tripOwnershipAndMemberAccess_shouldWork()
            throws Exception {

        String ownerToken =
                createVerifiedUserAndLogin();

        Long tripId =
                createTrip(
                        ownerToken,
                        "Shared Trip"
                );

        String editorEmail =
                "editor-" +
                        UUID.randomUUID() +
                        "@test.com";

        String editorToken =
                createVerifiedUserAndLogin(
                        editorEmail,
                        "Editor"
                );

        String addMemberRequest = """
                {
                    "email": "%s",
                    "role": "EDITOR"
                }
                """.formatted(editorEmail);

        mockMvc.perform(
                        post(
                                "/api/trips/{tripId}/members",
                                tripId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + ownerToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(addMemberRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email")
                        .value(editorEmail))
                .andExpect(jsonPath("$.data.role")
                        .value("EDITOR"));

        /*
         * Editor should see the shared trip in GET /api/trips.
         */
        mockMvc.perform(
                        get("/api/trips")
                                .header(
                                        "Authorization",
                                        "Bearer " + editorToken
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.content[*].id")
                                .value(
                                        hasItem(
                                                tripId.intValue()
                                        )
                                )
                );

        /*
         * Editor must not be able to delete the Trip.
         */
        mockMvc.perform(
                        delete("/api/trips/{id}", tripId)
                                .header(
                                        "Authorization",
                                        "Bearer " + editorToken
                                )
                )
                .andExpect(status().isNotFound());

        assertTrue(
                tripRepository.findById(tripId)
                        .isPresent()
        );
    }


    // =========================================================
    // 4. ITINERARY + ORPHAN REMOVAL
    // =========================================================

    @Test
    void itineraryUpdate_shouldRemoveOldDaysAndActivities()
            throws Exception {

        String token =
                createVerifiedUserAndLogin();

        Long tripId =
                createTrip(
                        token,
                        "Itinerary Test Trip"
                );

        String createItineraryRequest = """
                {
                    "summary": "Original itinerary",
                    "days": [
                        {
                            "dayNumber": 1,
                            "date": "2026-09-10",
                            "notes": "Day one",
                            "activities": [
                                {
                                    "title": "India Gate",
                                    "description": "Visit India Gate",
                                    "startTime": "09:00:00",
                                    "endTime": "10:30:00",
                                    "location": "India Gate",
                                    "estimatedCost": 0,
                                    "currency": "INR",
                                    "type": "SIGHTSEEING"
                                }
                            ]
                        },
                        {
                            "dayNumber": 2,
                            "date": "2026-09-11",
                            "notes": "Day two",
                            "activities": [
                                {
                                    "title": "Red Fort",
                                    "description": "Visit Red Fort",
                                    "startTime": "09:00:00",
                                    "endTime": "12:00:00",
                                    "location": "Red Fort",
                                    "estimatedCost": 500,
                                    "currency": "INR",
                                    "type": "SIGHTSEEING"
                                }
                            ]
                        },
                        {
                            "dayNumber": 3,
                            "date": "2026-09-12",
                            "notes": "Day three",
                            "activities": [
                                {
                                    "title": "Taj Mahal",
                                    "description": "Visit Taj Mahal",
                                    "startTime": "07:00:00",
                                    "endTime": "10:00:00",
                                    "location": "Agra",
                                    "estimatedCost": 1000,
                                    "currency": "INR",
                                    "type": "SIGHTSEEING"
                                }
                            ]
                        }
                    ]
                }
                """;

        String createResponse =
                mockMvc.perform(
                                post(
                                        "/api/trips/{tripId}/itinerary",
                                        tripId
                                )
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(
                                                createItineraryRequest
                                        )
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Number itineraryIdNumber =
                JsonPath.read(
                        createResponse,
                        "$.data.id"
                );

        Long itineraryId =
                itineraryIdNumber.longValue();

        Long oldDay3Id =
                ((Number) JsonPath.read(
                        createResponse,
                        "$.data.days[2].id"
                )).longValue();

        Long oldActivity3Id =
                ((Number) JsonPath.read(
                        createResponse,
                        "$.data.days[2].activities[0].id"
                )).longValue();

        String updateItineraryRequest = """
                {
                    "summary": "Updated itinerary",
                    "days": [
                        {
                            "dayNumber": 1,
                            "date": "2026-09-10",
                            "notes": "Updated day one",
                            "activities": [
                                {
                                    "title": "India Gate",
                                    "description": "Updated visit",
                                    "startTime": "08:00:00",
                                    "endTime": "09:30:00",
                                    "location": "India Gate",
                                    "estimatedCost": 0,
                                    "currency": "INR",
                                    "type": "SIGHTSEEING"
                                }
                            ]
                        },
                        {
                            "dayNumber": 2,
                            "date": "2026-09-11",
                            "notes": "Updated day two",
                            "activities": [
                                {
                                    "title": "Jama Masjid",
                                    "description": "Visit Jama Masjid",
                                    "startTime": "15:00:00",
                                    "endTime": "16:30:00",
                                    "location": "Delhi",
                                    "estimatedCost": 0,
                                    "currency": "INR",
                                    "type": "SIGHTSEEING"
                                }
                            ]
                        }
                    ]
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/trips/{tripId}/itinerary",
                                tripId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        updateItineraryRequest
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.id")
                                .value(itineraryId)
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value("MODIFIED")
                );

        assertTrue(
                itineraryDayRepository
                        .findById(oldDay3Id)
                        .isEmpty()
        );

        assertTrue(
                activityRepository
                        .findById(oldActivity3Id)
                        .isEmpty()
        );

        mockMvc.perform(
                        get(
                                "/api/trips/{tripId}/itinerary",
                                tripId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.days")
                                .value(hasSize(2))
                )
                .andExpect(
                        jsonPath("$.data.days[0].dayNumber")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.data.days[1].dayNumber")
                                .value(2)
                );
    }


    // =========================================================
    // 5. EXPENSE CRUD + FILTERING
    // =========================================================

    @Test
    void expenseCrudAndFiltering_shouldWork()
            throws Exception {

        String token =
                createVerifiedUserAndLogin();

        Long tripId =
                createTrip(
                        token,
                        "Expense Test Trip"
                );

        String expenseRequest = """
                {
                    "amount": 600,
                    "currency": "INR",
                    "category": "FOOD",
                    "description": "Lunch",
                    "expenseDate": "2026-09-10"
                }
                """;

        String createResponse =
                mockMvc.perform(
                                post(
                                        "/api/trips/{tripId}/expenses",
                                        tripId
                                )
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(expenseRequest)
                        )
                        .andExpect(status().isCreated())
                        .andExpect(
                                jsonPath("$.data.amount")
                                        .value(600)
                        )
                        .andExpect(
                                jsonPath("$.data.currency")
                                        .value("INR")
                        )
                        .andExpect(
                                jsonPath("$.data.category")
                                        .value("FOOD")
                        )
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Long expenseId =
                ((Number) JsonPath.read(
                        createResponse,
                        "$.data.id"
                )).longValue();

        // GET
        mockMvc.perform(
                        get(
                                "/api/trips/{tripId}/expenses",
                                tripId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.content")
                                .value(hasSize(1))
                );

        // FILTER BY CATEGORY
        mockMvc.perform(
                        get(
                                "/api/trips/{tripId}/expenses",
                                tripId
                        )
                                .param(
                                        "category",
                                        "FOOD"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.content")
                                .value(hasSize(1))
                );

        // UPDATE
        String updateExpenseRequest = """
                {
                    "amount": 750,
                    "currency": "INR",
                    "category": "FOOD",
                    "description": "Updated lunch",
                    "expenseDate": "2026-09-10"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/trips/{tripId}/expenses/{expenseId}",
                                tripId,
                                expenseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        updateExpenseRequest
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.amount")
                                .value(750)
                );

        // DELETE
        mockMvc.perform(
                        delete(
                                "/api/trips/{tripId}/expenses/{expenseId}",
                                tripId,
                                expenseId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());

        assertTrue(
                expenseRepository
                        .findById(expenseId)
                        .isEmpty()
        );
    }


    // =========================================================
    // HELPERS
    // =========================================================

    private String createVerifiedUserAndLogin()
            throws Exception {

        String email =
                "user-" +
                        UUID.randomUUID() +
                        "@test.com";

        return createVerifiedUserAndLogin(
                email,
                "Test"
        );
    }

    private String createVerifiedUserAndLogin(
            String email,
            String firstName
    ) throws Exception {

        String registerRequest = """
                {
                    "firstName": "%s",
                    "lastName": "User",
                    "email": "%s",
                    "password": "Password123!"
                }
                """.formatted(
                firstName,
                email
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(registerRequest)
                )
                .andExpect(status().isCreated());

        User user =
                userRepository
                        .findUserByEmail(email)
                        .orElseThrow();

        /*
         * Authentication integration tests already cover
         * email verification separately. For Trip tests,
         * activate the user directly so these tests focus
         * on Trip functionality.
         */
        user.verifyEmail();
        userRepository.save(user);

        String loginRequest = """
                {
                    "email": "%s",
                    "password": "Password123!"
                }
                """.formatted(email);

        String loginResponse =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(loginRequest)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return JsonPath.read(
                loginResponse,
                "$.data.accessToken"
        );
    }

    private Long createTrip(
            String token,
            String title
    ) throws Exception {

        String request = """
                {
                    "title": "%s",
                    "description": "Integration test trip",
                    "destination": "Delhi",
                    "startDate": "2026-09-10",
                    "endDate": "2026-09-12"
                }
                """.formatted(title);

        String response =
                mockMvc.perform(
                                post("/api/trips")
                                        .header(
                                                "Authorization",
                                                "Bearer " + token
                                        )
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(request)
                        )
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        Number tripId =
                JsonPath.read(
                        response,
                        "$.data.id"
                );

        return tripId.longValue();
    }
}