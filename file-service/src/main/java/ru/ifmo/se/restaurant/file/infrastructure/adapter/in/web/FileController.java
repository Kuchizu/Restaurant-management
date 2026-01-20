package ru.ifmo.se.restaurant.file.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ifmo.se.restaurant.file.application.dto.FileDownloadResult;
import ru.ifmo.se.restaurant.file.application.dto.FileResponse;
import ru.ifmo.se.restaurant.file.application.dto.FileUploadCommand;
import ru.ifmo.se.restaurant.file.application.port.in.DeleteFileUseCase;
import ru.ifmo.se.restaurant.file.application.port.in.DownloadFileUseCase;
import ru.ifmo.se.restaurant.file.application.port.in.GetFileMetadataUseCase;
import ru.ifmo.se.restaurant.file.application.port.in.UploadFileUseCase;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Management", description = "API for uploading, downloading and managing files")
public class FileController {

    private final UploadFileUseCase uploadFileUseCase;
    private final DownloadFileUseCase downloadFileUseCase;
    private final GetFileMetadataUseCase getFileMetadataUseCase;
    private final DeleteFileUseCase deleteFileUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file", description = "Upload a file with category. Only 'file' and 'category' are required.")
    public ResponseEntity<FileResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") FileCategory category,
            @Parameter(description = "Optional: ID of related entity (e.g., dish ID)", required = false)
            @RequestParam(value = "entityId", required = false) Long entityId,
            @Parameter(description = "Optional: User ID (auto-filled from JWT)", required = false, hidden = true)
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) throws IOException {
        FileUploadCommand command = FileUploadCommand.builder()
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .inputStream(file.getInputStream())
                .category(category)
                .entityId(entityId)
                .uploadedBy(userId)
                .build();

        FileResponse response = uploadFileUseCase.upload(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}")
    @Operation(summary = "Download a file", description = "Download a file by its ID")
    public ResponseEntity<InputStreamResource> download(@PathVariable String fileId) {
        FileDownloadResult result = downloadFileUseCase.download(fileId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(result.getContentType()))
                .contentLength(result.getFileSize())
                .body(new InputStreamResource(result.getInputStream()));
    }

    @GetMapping("/{fileId}/metadata")
    @Operation(summary = "Get file metadata", description = "Get metadata for a file by its ID")
    public ResponseEntity<FileResponse> getMetadata(@PathVariable String fileId) {
        FileResponse response = getFileMetadataUseCase.getById(fileId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get files by category", description = "Get all files for a specific category")
    public ResponseEntity<List<FileResponse>> getByCategory(@PathVariable FileCategory category) {
        List<FileResponse> files = getFileMetadataUseCase.getByCategory(category);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/entity/{entityId}")
    @Operation(summary = "Get files by entity ID", description = "Get all files associated with a specific entity")
    public ResponseEntity<List<FileResponse>> getByEntityId(@PathVariable Long entityId) {
        List<FileResponse> files = getFileMetadataUseCase.getByEntityId(entityId);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Delete a file", description = "Delete a file by its ID")
    public ResponseEntity<Void> delete(@PathVariable String fileId) {
        deleteFileUseCase.delete(fileId);
        return ResponseEntity.noContent().build();
    }
}
