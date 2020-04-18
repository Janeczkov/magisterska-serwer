package com.example.serwer.controller;

import com.example.serwer.exception.ResourceNotFoundException;
import com.example.serwer.model.Accounts;
import com.example.serwer.payload.Uploads;
import com.example.serwer.property.FileStorageProperties;
import com.example.serwer.repository.AccountsRepository;
import com.example.serwer.repository.UploadsRepository;
import com.example.serwer.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);


    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UploadsRepository uploadsRepository;
    @Autowired
    private AccountsRepository accountsRepository;



    @PostMapping("/upload")
    public Uploads uploadFile(@RequestParam("content") MultipartFile file,
                              @RequestParam("autor") String author) {
        String fileName = fileStorageService.storeFile(file);


        Uploads upload = new Uploads(fileName,
                file.getContentType(), file.getSize(), author);

        return uploadsRepository.save(upload);


    }

    @PostMapping("/uploadMultiple")
    public List<Uploads> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files,
                                             @RequestParam("typ") String typ,
                                             @RequestParam("kategoria") String category,
                                             @RequestParam("autor") String author) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file, author))
                .collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<Uploads> getAllFiles() {
        return uploadsRepository.findAll();
    }

    /*public List<Uploads> findByExample(Uploads upload) {
        return
    }*/

    @PutMapping("/update/{id}")
    public Uploads updateFile(@PathVariable(value = "id") Long uploadId,
                                  @Valid @RequestBody Uploads updateDetails) {

        Uploads upload = uploadsRepository.findById(uploadId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", uploadId));

        //
        if (updateDetails.isAccepted()) {
            try {
                upload.setAccepted(true);
                String author = upload.getAuthor();

                Accounts account = accountsRepository.findByUsername(author).get(0);
                account.setElements(account.getElements() + 1);
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
        }


        Uploads updatedUploads = uploadsRepository.save(upload);
        return updatedUploads;
    }

    @GetMapping("/download/{uploadId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long uploadId) {
        // Load file as Resource

        Uploads upload = uploadsRepository.findById(uploadId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", uploadId));


        Resource resource = fileStorageService.loadFileAsResource(upload.getFileName());


        String contentType = "application/octet-stream";


        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable(value = "id") Long uploadId,
                                           FileStorageProperties fileStorageProperties) {
        Uploads upload = uploadsRepository.findById(uploadId)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", uploadId));

        uploadsRepository.delete(upload);

        String fileStorageLocation;

        fileStorageLocation = fileStorageProperties.getUploadDir();

        String filename = upload.getFileName();
        File file = new File(fileStorageLocation, filename);

        file.delete();

        return ResponseEntity.ok().build();
    }
}