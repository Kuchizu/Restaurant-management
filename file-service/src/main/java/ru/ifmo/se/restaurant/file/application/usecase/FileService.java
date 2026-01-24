package ru.ifmo.se.restaurant.file.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.se.restaurant.file.application.dto.FileDownloadResult;
import ru.ifmo.se.restaurant.file.application.dto.FileResponse;
import ru.ifmo.se.restaurant.file.application.dto.FileUploadCommand;
import ru.ifmo.se.restaurant.file.application.port.in.DeleteFileUseCase;
import ru.ifmo.se.restaurant.file.application.port.in.DownloadFileUseCase;
import ru.ifmo.se.restaurant.file.application.port.in.GetFileMetadataUseCase;
import ru.ifmo.se.restaurant.file.application.port.in.UploadFileUseCase;
import ru.ifmo.se.restaurant.file.application.port.out.FileEventPublisher;
import ru.ifmo.se.restaurant.file.application.port.out.FileMetadataRepository;
import ru.ifmo.se.restaurant.file.application.port.out.FileStoragePort;
import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;
import ru.ifmo.se.restaurant.file.domain.exception.FileNotFoundException;
import ru.ifmo.se.restaurant.file.domain.exception.InvalidFileException;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileService implements UploadFileUseCase, DownloadFileUseCase, GetFileMetadataUseCase, DeleteFileUseCase {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final FileStoragePort fileStorage;
    private final FileMetadataRepository metadataRepository;
    private final FileEventPublisher eventPublisher;

    @Override
    public FileResponse upload(FileUploadCommand command) {
        validateFile(command);

        String fileId = UUID.randomUUID().toString();
        String storedName = generateStoredName(fileId, command.getOriginalFileName());

        log.info("Uploading file: {} as {}", command.getOriginalFileName(), storedName);

        String fileUrl = fileStorage.store(
                storedName,
                command.getInputStream(),
                command.getContentType(),
                command.getFileSize()
        );

        FileMetadata metadata = FileMetadata.builder()
                .id(fileId)
                .originalName(command.getOriginalFileName())
                .storedName(storedName)
                .contentType(command.getContentType())
                .fileSize(command.getFileSize())
                .bucket(getBucketForCategory(command.getCategory()))
                .fileUrl(fileUrl)
                .category(command.getCategory())
                .entityId(command.getEntityId())
                .uploadedAt(Instant.now())
                .uploadedBy(command.getUploadedBy())
                .build();

        FileMetadata saved = metadataRepository.save(metadata);
        eventPublisher.publishFileUploaded(saved);

        log.info("File uploaded successfully: {}", fileId);
        return FileResponse.fromDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadResult download(String fileId) {
        FileMetadata metadata = metadataRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        InputStream inputStream = fileStorage.retrieve(metadata.getStoredName());

        return FileDownloadResult.builder()
                .fileName(metadata.getOriginalName())
                .contentType(metadata.getContentType())
                .fileSize(metadata.getFileSize())
                .inputStream(inputStream)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FileResponse getById(String fileId) {
        return metadataRepository.findById(fileId)
                .map(FileResponse::fromDomain)
                .orElseThrow(() -> new FileNotFoundException(fileId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileResponse> getByCategory(FileCategory category) {
        return metadataRepository.findByCategory(category).stream()
                .map(FileResponse::fromDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileResponse> getByEntityId(Long entityId) {
        return metadataRepository.findByEntityId(entityId).stream()
                .map(FileResponse::fromDomain)
                .toList();
    }

    @Override
    public void delete(String fileId) {
        FileMetadata metadata = metadataRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException(fileId));

        log.info("Deleting file: {}", fileId);

        fileStorage.delete(metadata.getStoredName());
        metadataRepository.deleteById(fileId);

        log.info("File deleted successfully: {}", fileId);
    }

    private void validateFile(FileUploadCommand command) {
        if (command.getFileSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File size exceeds maximum allowed size of 10MB");
        }

        if (command.getCategory() == FileCategory.DISH_IMAGE ||
                command.getCategory() == FileCategory.CATEGORY_IMAGE) {
            if (!ALLOWED_IMAGE_TYPES.contains(command.getContentType())) {
                throw new InvalidFileException("Invalid image type. Allowed types: JPEG, PNG, WebP, GIF");
            }
        }
    }

    private String generateStoredName(String fileId, String originalName) {
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        return fileId + extension;
    }

    private String getBucketForCategory(FileCategory category) {
        return switch (category) {
            case DISH_IMAGE, CATEGORY_IMAGE -> "dish-images";
            case RECEIPT, REPORT -> "documents";
            default -> "general";
        };
    }
}
