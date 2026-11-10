package com.example.recruitment_service.Application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record JobApplicationRequest(

        @NotNull(message = "L'identifiant de l'offre est obligatoire.")
        UUID offerId,

        @Size(max = 4000, message = "La lettre de motivation ne doit pas dépasser 4000 caractères.")
        String coverLetter
) {}