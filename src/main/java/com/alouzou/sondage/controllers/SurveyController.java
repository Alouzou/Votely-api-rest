package com.alouzou.sondage.controllers;

import com.alouzou.sondage.dto.SurveyDTO;
import com.alouzou.sondage.entities.Survey;
import com.alouzou.sondage.entities.User;
import com.alouzou.sondage.repositories.SurveyRepository;
import com.alouzou.sondage.repositories.UserRepository;
import com.alouzou.sondage.services.SurveyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/surveys/")
public class SurveyController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SurveyService surveyService;

    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    @PostMapping("/create")
    public ResponseEntity<SurveyDTO> createSurvey(@Valid @RequestBody SurveyDTO surveyDTO) {
        Survey createdSurvey = surveyService.createSurvey(surveyDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(SurveyDTO.fromEntity(createdSurvey));
    }

    @GetMapping("/my-surveys")
    public ResponseEntity<List<SurveyDTO>> getMySurveys(Authentication authentication) {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + username));

        List<SurveyDTO> surveys = surveyService.getSurveysByCreator(user.getId())
                .stream()
                .map(SurveyDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(surveys);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurveyDTO> getSurveyById(@PathVariable Long id) {
        return surveyService.getSurveyById(id)
                .map(SurveyDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<SurveyDTO>> getSurveysByCategory(@PathVariable Long categoryId) {
        List<SurveyDTO> surveys = surveyService.getSurveysByCategory(categoryId)
                .stream()
                .map(SurveyDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(surveys);

    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<SurveyDTO>> getSurveysByCreator(@PathVariable Long creatorId) {
        List<SurveyDTO> surveys = surveyService.getSurveysByCreator(creatorId)
                .stream()
                .map(SurveyDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(surveys);
    }

    @GetMapping("")
    public ResponseEntity<List<SurveyDTO>> findAll() {
        List<SurveyDTO> surveys = surveyService.findAll()
                .stream()
                .map(SurveyDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity
                .ok(surveys);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{idSurvey}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable("idSurvey") Long idSurvey) {
        surveyService.deleteSurvey(idSurvey);
        return ResponseEntity.noContent().build();

    }
}
