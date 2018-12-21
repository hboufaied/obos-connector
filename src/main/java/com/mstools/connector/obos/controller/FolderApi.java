package com.mstools.connector.obos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-10-22T08:34:14.629Z")
@Api(value = "/api/storage", description = "the OBOS connector API")
@RequestMapping("/api/storage")
public interface FolderApi {

	@ApiOperation(value = "", nickname = "Delete file from OBOS folder", notes = "", response = Void.class, tags = {})
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "200 response", response = Void.class),
			@ApiResponse(code = 400, message = "400 response"), 
			@ApiResponse(code = 500, message = "500 response") 
			})
	@DeleteMapping(value = "/{item}", produces = { "application/json" })
	ResponseEntity<String> folderItemDelete(@ApiParam(value = "", required = true) @PathVariable("item") String item);

	@ApiOperation(value = "", nickname = "Delete file from root OBOS", notes = "", response = Void.class, tags = {})
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "200 response", response = Void.class),
			@ApiResponse(code = 400, message = "400 response"), 
			@ApiResponse(code = 500, message = "500 response") 
			})
	@DeleteMapping(value = "/{folder}/{item}", produces = { "application/json" })
	ResponseEntity<String> folderItemDelete(@ApiParam(value = "", required = true) @PathVariable("folder") String folder, @ApiParam(value = "", required = true) @PathVariable("item") String item);
	
	@ApiOperation(value = "", nickname = "Delete item from OBOS", notes = "", response = Void.class, tags = {})
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "200 response", response = Void.class),
			@ApiResponse(code = 400, message = "400 response"), 
			@ApiResponse(code = 500, message = "500 response") })
	@GetMapping(value = "/{folder}/{item}", produces = { "application/json" })
	ResponseEntity<?> folderItemGet(@ApiParam(value = "", required = true) @PathVariable("folder") String folder,
			@ApiParam(value = "", required = true) @PathVariable("item") String item);

	@ApiOperation(value = "", nickname = "Save file into OBOS", notes = "", response = Void.class, tags = {})
	@ApiResponses(value = { 
			@ApiResponse(code = 201, message = "201 response", response = Void.class),
			@ApiResponse(code = 400, message = "400 response"), 
			@ApiResponse(code = 500, message = "500 response") })
	@PostMapping(value = "/{folder}", produces = { "application/json" })
	ResponseEntity<?> uploadFile(@ApiParam(value = "", required = true) @PathVariable("folder") String folder,
			@ApiParam(value = "", required = true) @RequestPart(value = "file") MultipartFile file);

}