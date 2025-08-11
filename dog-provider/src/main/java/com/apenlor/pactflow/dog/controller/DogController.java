package com.apenlor.pactflow.dog.controller;

import com.apenlor.pactflow.dog.dto.BreedsResponse;
import com.apenlor.pactflow.dog.dto.RandomDogImage;
import com.apenlor.pactflow.dog.service.DogCeoService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dogs")
@Slf4j
public class DogController {

    private final DogCeoService dogCeoService;

    public DogController(DogCeoService dogCeoService) {
        this.dogCeoService = dogCeoService;
    }

    @GetMapping()
    public BreedsResponse getHoundSubBreeds() {
        log.info("Fetching sub-breeds for hound");
        return dogCeoService.getHoundSubBreeds();
    }

    @GetMapping("/random")
    public RandomDogImage getRandomDogImage() {
        log.info("Random dog image requested");
        return dogCeoService.getRandomDogImage();
    }
}
