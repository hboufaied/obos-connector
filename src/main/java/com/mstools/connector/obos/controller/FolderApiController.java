package com.mstools.connector.obos.controller;

import java.io.ByteArrayOutputStream;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mstools.connector.obos.exception.ObosConnectorException;
import com.mstools.connector.obos.model.UploadResult;
import com.mstools.connector.obos.service.AmazonClient;

import lombok.extern.slf4j.Slf4j;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-10-22T08:34:14.629Z")

@RestController
@Slf4j
public class FolderApiController implements FolderApi {

	private AmazonClient amazonClient;

	@Value("${aws.bucketName}")
	private String bucketName;

	@Autowired
	FolderApiController(AmazonClient amazonClient) {
		this.amazonClient = amazonClient;
	}

	@Override
	public ResponseEntity<?> uploadFile(@PathVariable("folder") String folder,
			@RequestPart(value = "file") MultipartFile file) {

		String result;
		URI uri = null;
		try {
			if (file.isEmpty()) {
				throw new ObosConnectorException(1000, "file was empty");
			}

			log.debug("Upload file {} in folder {}", file.getOriginalFilename(), folder);
			result = amazonClient.uploadFile(bucketName, folder, file);
			
			// return the header content-location if file is uploaded successfully
	           String template = "/api/v1/sms/campaigns/{id}";
	            uri = ServletUriComponentsBuilder.fromCurrentServletMapping().path(template).build(result);

		} catch (ObosConnectorException e) {
			log.error("Error sending the file to obos server", e);
			return new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
		}

		return ResponseEntity.created(uri).body(new UploadResult(result));
	}

	 @Override
	 public ResponseEntity<String> folderItemDelete(@PathVariable("folder") String folder, @PathVariable("item") String item) {
		 log.debug("Delete file {} from folder {}", item, folder);
		try {
			amazonClient.deleteFileFromS3Bucket(bucketName, folder + "/" + item);
		} catch (ObosConnectorException e) {
			log.error("Error deleting the file from obos server", e);
			return new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
		}
		 return ResponseEntity.noContent().build();
	 }
	
	 @Override
	 public ResponseEntity<String> folderItemDelete(@PathVariable("item") String item) {
		 log.debug("Delete file {} from root obos", item);
		try {
			amazonClient.deleteFileFromS3Bucket(bucketName, item);
		} catch (ObosConnectorException e) {
			log.error("Error deleting the file from obos server", e);
			return new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
		}
		 return ResponseEntity.noContent().build();
	 }
	 
	@Override
	public ResponseEntity<?> folderItemGet(@PathVariable("folder") String folder, @PathVariable("item") String item) {
		
		log.debug("Download file {} from folder {}", item, folder);
		try {
			ByteArrayOutputStream downloadInputStream = amazonClient.downloadFile(bucketName, folder + "/" + item);
			return ResponseEntity.ok()
					.contentType(MediaType.IMAGE_PNG)
					.header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + item + "\"")
					.body(downloadInputStream.toByteArray());
		} catch (ObosConnectorException e) {
			log.error("Error downloading the file to obos server", e);
			return new ResponseEntity<>(e.toString(), HttpStatus.BAD_REQUEST);
		}
	}

}