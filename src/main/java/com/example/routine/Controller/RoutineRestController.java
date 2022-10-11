package com.example.routine.Controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;


import com.example.routine.DTO.EventDto;
import com.example.routine.DTO.ParticipantDto;
import com.example.routine.DTO.ParticipantFullDto;
import com.example.routine.Model.ParticipantStatus;
import com.example.routine.Repository.EventRepository;
import com.example.routine.Repository.ParticipantRepository;
import com.example.routine.exception.EventNotFoundException;
import com.example.routine.exception.ParticipantNotFoundException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/routine")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class RoutineRestController {

    private EventRepository eventRepository;
    private ParticipantRepository participantRepository;
    private ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<ParticipantDto>> findAll() {
        return ResponseEntity.ok(participantRepository.findParticipantByStatus(ParticipantStatus.ACTIVE).stream().
                map(participant -> modelMapper.map(participant, ParticipantDto.class)).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<ParticipantDto> addParticipant(@Valid @RequestBody ParticipantDto participantDto) {
        System.out.println(participantDto);
        var participant = participantDto.toParticipant();
        return ResponseEntity.ok(modelMapper.map(participantRepository.save(participant), ParticipantDto.class));
    }

    @GetMapping("/{participantId}")
    public ResponseEntity<ParticipantFullDto> getWithEvents(@PathVariable Long participantId) {
        var participant = participantRepository.findByIdAndStatus(participantId, ParticipantStatus.ACTIVE).
                orElseThrow(() -> new ParticipantNotFoundException(participantId));
        return ResponseEntity.ok(modelMapper.map(participant, ParticipantFullDto.class));
    }

    @DeleteMapping("/{participantId}")
    public ResponseEntity<String> deleteParticipant(@PathVariable Long participantId) {
        var participant = participantRepository.findByIdAndStatus(participantId, ParticipantStatus.ACTIVE).
                orElseThrow(() -> new ParticipantNotFoundException(participantId));
        participant.setStatus(ParticipantStatus.REMOVED);
        participant.setEvents(null);
        participantRepository.save(participant);
        return ResponseEntity.ok("deleted");
    }

    @PatchMapping()
    public ResponseEntity<ParticipantDto> changeParticipant(@Valid @RequestBody ParticipantDto participantDto) {
        var participant = participantRepository.findByIdAndStatus(participantDto.getId(), ParticipantStatus.ACTIVE).
                orElseThrow(() -> new ParticipantNotFoundException(participantDto.getId()));
        participant.setLastName(participantDto.getLastName());
        participant.setFirstName(participantDto.getFirstName());
        participantRepository.save(participant);
        return ResponseEntity.ok(participantDto);
    }

    @PostMapping("/{participantId}/events")
    public ResponseEntity<EventDto> addEvent(@PathVariable Long participantId, @Valid @RequestBody EventDto eventDto) {
        var event = eventDto.toEvent();
        var participant = participantRepository.findById(participantId).orElseThrow(() -> new ParticipantNotFoundException(participantId));
        participant.addEvent(event);
        var p = participantRepository.save(participant);
        var events = p.getEvents();
        return ResponseEntity.ok(modelMapper.map(events.get(events.size() -1), EventDto.class));

    }
    @PatchMapping("/events")
    public ResponseEntity<@Valid EventDto> changeEvent(@Valid @RequestBody EventDto eventDto) {
        var event = eventRepository.findById(eventDto.getId()).orElseThrow(() -> new EventNotFoundException(eventDto.getId()));
        event.setDescription(eventDto.getDescription());
        event.setStartTime(eventDto.getStartTime());
        event.setEndTime(eventDto.getEndTime());
        return ResponseEntity.ok(modelMapper.map(eventRepository.save(event), EventDto.class));
    }
    @DeleteMapping("/{participantId}/events/{eventId}")
    public void deleteEvent(@PathVariable Long participantId, @PathVariable Long eventId) {
        var participant = participantRepository.findById(participantId).orElseThrow(() -> new ParticipantNotFoundException(participantId));
        participant.setEvents(participant.getEvents().stream().filter(event -> !event.getId().equals(eventId)).toList());
        participantRepository.save(participant);
    }

}
