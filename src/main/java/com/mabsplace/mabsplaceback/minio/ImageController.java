package com.mabsplace.mabsplaceback.minio;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/images")
public class ImageController {

  private final MinioService minioService;

  public ImageController(MinioService minioService) {
    this.minioService = minioService;
  }

  @PostMapping
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
    try {
      minioService.uploadImage(file.getOriginalFilename(), file.getInputStream(), file.getContentType());
      return ResponseEntity.ok("Image uploaded successfully");
    } catch (IOException e) {
      return ResponseEntity.status(500).body("Failed to upload image");
    }
  }

  @GetMapping("/{imageName}")
//  @PreAuthorize("hasAuthority('ROLE_ADMIN')or hasAuthority('ROLE_USER')")
  public ResponseEntity<byte[]> downloadImage(@PathVariable String imageName) {
    try {
      InputStream imageStream = minioService.downloadImage(imageName);
      // Convert the InputStream to a byte array and return as ResponseEntity
      // Remember to set the appropriate Content-Type header based on your image type.
      // For example, for JPEG images, use MediaType.IMAGE_JPEG
      // For PNG images, use MediaType.IMAGE_PNG
      // For other types, adjust accordingly.
      byte[] imageBytes = IOUtils.toByteArray(imageStream);

      return ResponseEntity.ok()
              .contentType(MediaType.IMAGE_JPEG) // Change based on your image type
              .body(imageBytes);
    } catch (Exception e) {
      return ResponseEntity.status(404).body(null);
    }
  }

}