package com.apenlor.pactflow.dog.service;

import com.apenlor.pactflow.dog.dto.BreedsResponse;
import com.apenlor.pactflow.dog.dto.RandomDogImage;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class DogCeoService {

    private final WebClient webClient;

    public DogCeoService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://dog.ceo/api").build();
    }

    public BreedsResponse getHoundSubBreeds() {
        return webClient.get()
                .uri("/breed/hound/list")
                .retrieve()
                .bodyToMono(BreedsResponse.class).block();
    }

    public RandomDogImage getRandomDogImage() {
        return webClient.get()
                .uri("/breeds/image/random")
                .retrieve()
                .bodyToMono(RandomDogImage.class).block();
    }
}