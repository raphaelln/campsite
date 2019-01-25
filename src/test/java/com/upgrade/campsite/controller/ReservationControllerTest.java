package com.upgrade.campsite.controller;

import com.google.gson.*;
import com.upgrade.campsite.CampsiteApplication;
import com.upgrade.campsite.presenter.BookingData;
import com.upgrade.campsite.repository.ReservationRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CampsiteApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles( "test" )
public class ReservationControllerTest {

    private final String reservationPath = "/v1/reservation/";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RedisTemplate<String, LocalDate> template;

    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @After
    public void after() throws Exception {
        reservationRepository.deleteAll();
        template.delete("reservations");
    }

    @Test
    public void testDeleteReservationNotFound() throws Exception {
        mvc.perform(delete(String.format("%s%s", reservationPath, "2222"))).andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteReservationSuccess() throws Exception {

        MvcResult result = mvc.perform(post(reservationPath)
                .content(getPayload(LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)))
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        final String transactionId = result.getResponse().getContentAsString();
        mvc.perform(delete(String.format("%s%s", reservationPath, transactionId))).andExpect(status().isNoContent());
    }

    @Test
    public void testCreateReservationWithBusinessError() throws Exception {
        mvc.perform(post(reservationPath)
                .content(getPayload(LocalDate.now().plusDays(2), LocalDate.now().plusDays(15)))
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The campsite only be booked up to 3 days."));
    }

    @Test
    public void testSuccessfulCreateReservation() throws Exception {
        mvc.perform(post(reservationPath)
                .content(getPayload(LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)))
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testModifyReservationWithInvalidTransactionId() throws Exception {
        mvc.perform(put(String.format("%s%s", reservationPath, "123"))
                .content(getPayload(LocalDate.now().plusDays(2), LocalDate.now().plusDays(15)))
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateReservationWithBusinessError() throws Exception {
        MvcResult result = mvc.perform(post(reservationPath)
                .content(getPayload(LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)))
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        final String transactionId = result.getResponse().getContentAsString();


        mvc.perform(put(String.format("%s%s", reservationPath, transactionId))
                .content(getPayload(LocalDate.now(), LocalDate.now().plusDays(1)))
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The campsite only be booked with a " +
                        "time limit of 1 day ahead or up to 1 month in advance."));
    }

    @Test
    public void testSuccessfulModifyReservation() throws Exception {

        MvcResult result = mvc.perform(post(reservationPath)
                .content(getPayload(LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)))
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        final String transactionId = result.getResponse().getContentAsString();

        mvc.perform(put(String.format("%s%s", reservationPath, transactionId))
                .content(getPayload(LocalDate.now().plusDays(5), LocalDate.now().plusDays(7)))
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testAvailabilityData() throws Exception {
        mvc.perform(post(reservationPath)
                .content(getPayload(LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)))
                .accept(MediaType.ALL)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        mvc.perform(get(
                String.format("%s/list-availability?startDate=%s&finalDate=%s",
                        reservationPath,
                        LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        LocalDate.now().plusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE))))
                .andExpect(status().isOk());
    }

    @Test
    public void testConcurrentCreateReservations() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Callable<Integer>> tasks = Arrays.asList(
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation(),
                () -> makeReservation()
        );

        long reservations = executor.invokeAll(tasks)
            .stream()
            .mapToInt(future -> {
                int result = 0;
                try {
                    if (future.get().equals(200)) {
                        result = 1;
                    }
                } catch (Exception e) { }
                return result;
            }).sum();
        executor.shutdown();
        assertEquals(1, reservations);
    }

    private Integer makeReservation() throws Exception {
        MvcResult result = mvc.perform(post(reservationPath)
            .content(getPayload(LocalDate.now().plusDays(2), LocalDate.now().plusDays(3)))
            .accept(MediaType.ALL)
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();
       return result.getResponse().getStatus();
    }

    private String getPayload(LocalDate startDate, LocalDate endDate) {
        BookingData data = BookingData.builder()
                .name("Elon musk")
                .email("elon.musk@tesla.com")
                .checkIn(startDate)
                .checkOut(endDate)
                .build();
        return getGson().toJson(data);
    }
    private Gson getGson() {
        return new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
    }

    static class LocalDateAdapter implements JsonSerializer<LocalDate> {
        public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    }
}