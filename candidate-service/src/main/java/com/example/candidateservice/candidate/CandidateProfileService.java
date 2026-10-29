package com.example.candidateservice.candidate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Slf4j

public class CandidateProfileService {
 private final CandidateProfileRepository candidateProfileRepository;
 private  final Path cvStorageDirectory;
    public CandidateProfileService(
            CandidateProfileRepository candidateProfileRepository,
            @Value("${app.cv-storage-directory:uploads/cvs}") String cvStorageDirectory
    ) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.cvStorageDirectory = Paths.get(cvStorageDirectory).toAbsolutePath().normalize();
    }

@Transactional
    public CandidateProfileResponse create(String id, CandidateProfileRequest request){
        requireText(id, "L'identifiant du candidat est obligatoire.");
        validateRequest(request);
        if(candidateProfileRepository.findById(id).isPresent()){
            throw new ResponseStatusException(CONFLICT, "Un profil candidat existe deja : " + id);
        }
        ensureEmailIsAvailable(request.email(),  null);
        CandidateProfile candidateProfile = CandidateProfile.builder()
                .id(id)
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(request.email().trim())
                .currentPosition(trimToNull(request.currentPosition()))
                .isLookingForJob(Boolean.TRUE.equals(request.lookingForJob()))
                .build();

        return toResponse(candidateProfileRepository.save(candidateProfile));
}


@Transactional(readOnly = true)
public List<CandidateProfileResponse> findAll(Boolean lookingForJob){
        return candidateProfileRepository.findAll().stream()
                .filter(cv -> lookingForJob == null || cv.isLookingForJob()== lookingForJob)
                .map(CandidateProfileResponse::fromEntity)
                .toList();
}

@Transactional(readOnly = true)
public CandidateProfileResponse findById(String id){
        return CandidateProfileResponse.fromEntity(getCandidateProfile(id));
}

@Transactional
public CandidateProfileResponse update(String id, CandidateProfileRequest request){
        validateRequest(request);
        CandidateProfile candidateProfile = getCandidateProfile(id);
        ensureEmailIsAvailable(request.email(),  id);
        candidateProfile.setFirstName(request.firstName().trim());
        candidateProfile.setLastName(request.lastName().trim());
        candidateProfile.setEmail(request.email().trim());
        candidateProfile.setCurrentPosition(trimToNull(request.currentPosition()));
        return toResponse(candidateProfileRepository.save(candidateProfile));

}

@Transactional
public void delete (String id){
        CandidateProfile candidateProfile = getCandidateProfile(id);
        candidateProfileRepository.delete(candidateProfile);
}

@Transactional
public CandidateProfileResponse uploadCv(String id, MultipartFile file){
        CandidateProfile candidateProfile = getCandidateProfile(id);
        if(file==null || file.isEmpty()){
            throw new ResponseStatusException(BAD_REQUEST, "le fichier CV est obligatoire.");

        }

        String extension=StringUtils.getFilenameExtension(file.getOriginalFilename());
        if(extension==null || !(extension.equalsIgnoreCase("pdf") || extension.equalsIgnoreCase("doc")
                || extension.equalsIgnoreCase("docx"))){
            throw new ResponseStatusException(BAD_REQUEST, "le CV doit etre au format PDF, DOC, ou DOCX.");
        }

        try {
            Files.createDirectories(cvStorageDirectory);
            Path target=cvStorageDirectory.resolve(UUID.randomUUID()+"."+extension.toLowerCase())
                    .normalize();
            if(!target.startsWith(cvStorageDirectory)){
                throw new ResponseStatusException(BAD_REQUEST, "Nom de fichier invalide.");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            candidateProfile.setCvFilePath(target.toString());
            return toResponse(candidateProfileRepository.save(candidateProfile));

        }
        catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Impossible d'enregistrer le fichier.", e);
        }
}

private CandidateProfile getCandidateProfile(String id){
        return candidateProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Candidat introuvable : " + id));
}

private void validateRequest(CandidateProfileRequest request){
        if(request == null || !StringUtils.hasText(request.firstName())
        || !StringUtils.hasText(request.lastName())
        || !StringUtils.hasText(request.email()))
            {
            throw new ResponseStatusException(BAD_REQUEST,"Les champs firstName, lastName et email sont obligatoires.");
        }
}

private void ensureEmailIsAvailable(String email, String currentId){
        boolean exists= currentId==null
                ? candidateProfileRepository.existsByEmailIgnoreCase(email.trim())
                : candidateProfileRepository.existsByEmailIgnoreCaseAndIdNot(email.trim(), currentId);
        if(exists){
            throw new ResponseStatusException(CONFLICT, "cet email existe deja.");
        }
}

private static void requireText(String value, String message){
        if(!StringUtils.hasText(value)){
            throw new ResponseStatusException(BAD_REQUEST, message);
        }
}

private static String trimToNull(String value){
        return StringUtils.hasText(value)? value.trim(): null;
}

private static CandidateProfileResponse toResponse(CandidateProfile candidateProfile){
        return CandidateProfileResponse.fromEntity(candidateProfile);
}

}
