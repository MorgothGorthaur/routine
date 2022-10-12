package com.example.routine.Controller;


import com.example.routine.DTO.EventDto;
import com.example.routine.DTO.ParticipantDto;
import com.example.routine.Model.ParticipantStatus;
import com.example.routine.Repository.EventRepository;
import com.example.routine.Repository.ParticipantRepository;
import com.example.routine.Service.EventService;
import com.example.routine.exception.ParticipantNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(RoutineRestController.class)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
class RestExceptionHandlerTest {
    @MockBean
    private EventRepository eventRepository;
    @MockBean
    private ParticipantRepository participantRepository;
    @MockBean
    private ModelMapper modelMapper;
    @MockBean
    private EventService eventService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mock;
    @Test
    void handleEntityNotFoundEx() throws Exception {
        when(participantRepository.findParticipantByStatus(ParticipantStatus.ACTIVE)).thenThrow(new ParticipantNotFoundException(1L));
        this.mock.perform(get("/routine/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("debugMessage", equalTo("Participant is not found, id=1")));
    }

    @Test
    void handleInvalidArgument() throws Exception {
        var dto = new ParticipantDto();

        this.mock.perform(post("/routine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", equalTo("validation error")));
    }

    @Test
    void handleHttpMessageNotReadable() throws Exception {
        this.mock.perform(patch("/routine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("ddd"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", equalTo("Malformed JSON Request")));
    }

    @Test
    void handleMethodArgumentTypeMismatchException() throws Exception {

        this.mock.perform(get("/routine/ddd"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("message", equalTo("The parameter 'participantId' of value 'ddd' could not be converted to type 'Long'")));
    }

    @Test
    void handleNoHandlerFoundException() throws Exception {
        this.mock.perform(get("/ddd"))
                .andExpect(status().isNotFound());
    }
}