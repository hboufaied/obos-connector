package com.mstools.connector.obos.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.mstools.connector.obos.exception.ObosConnectorException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AmazonClient {

	private AmazonS3 s3client;

	@Value("${aws.endpointUrl}")
	private String endpointUrl;


	@Value("${aws.accessKey}")
	private String accessKey;

	@Value("${aws.secretKey}")
	private String secretKey;
	
	@Value("${aws.withProxy}")
	private boolean withProxy;
	
	@Value("${aws.proxyHost}")
	private String proxyHost;
	
	@Value("${aws.proxyPort}")
	private int proxyPort;
	
	@Value("${aws.proxyUsername}")
	private String proxyUsername;
	
	@Value("${aws.proxyPassword}")
	private String proxyPassword;

	@PostConstruct
	private void initializeAmazon() {

		// First, we need to create a client connection to access Amazon S3 web service.
		// We’ll use AmazonS3 interface for this purpose:
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		
		// Set AWS configuration
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		clientConfiguration.setConnectionTimeout(5*1000*8);
		clientConfiguration.setSocketTimeout(5*1000*8);
		log.debug("Configure client S3 with proxy true/false : {}", withProxy);
		if(withProxy) {
	        clientConfiguration.setProxyHost(proxyHost);
	        clientConfiguration.setProxyPort(proxyPort);
			if (proxyUsername != null && proxyPassword != null && !proxyUsername.isEmpty()
					&& !proxyPassword.isEmpty()) {
				clientConfiguration.setProxyUsername(proxyUsername);
				clientConfiguration.setProxyPassword(proxyPassword);
			}
		}

		
		// And then configure the client
		this.s3client = new AmazonS3Client(credentials, clientConfiguration);
		this.s3client.setEndpoint(endpointUrl);
	}

	/**
	 * Upload file into OBOS server
	 * @param bucketName
	 * @param filePathName
	 * @param multipartFile
	 * @return
	 * @throws ObosConnectorException
	 */
	public String uploadFile(String bucketName, String filePathName, MultipartFile multipartFile) throws ObosConnectorException {
		String fileUrl = "";
		try {
			File file = convertMultiPartToFile(multipartFile);
			String fileName = generateFileName(multipartFile, filePathName);
			log.debug("Upload file into obos folder {}", fileName);
			fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
			uploadFile(bucketName, fileName, file);
			file.delete();
		} catch (Exception e) {
			throw new ObosConnectorException(1000, e.getMessage(), e.toString());
		}
		return fileUrl;
	}

	/**
	 * Delete file from OBOS server
	 * @param bucketName
	 * @param fileUrl
	 * @return
	 * @throws ObosConnectorException
	 */
	public String deleteFileFromS3Bucket(String bucketName, String fileUrl) throws ObosConnectorException {
		try {
			s3client.deleteObject(new DeleteObjectRequest(bucketName, fileUrl));
		} catch (AmazonServiceException ase) {
			log.error("Error from S3 server when we try to delete a file.", ase);
			throw new ObosConnectorException(ase.getStatusCode(), ase.getErrorCode(), ase.getMessage());
		} catch (AmazonClientException e) {
			log.error("Caught an Exception, which means the client encountered an internal error when downling a file.", e);
			throw new ObosConnectorException(1200, e.getMessage(), e.toString());
		}
		return "Successfully deleted";
	}

	public ByteArrayOutputStream  downloadFile(String bucketName, String fileUrl) throws ObosConnectorException {

		try {
            S3Object s3object = s3client.getObject(new GetObjectRequest(bucketName, fileUrl));
            
            InputStream is = s3object.getObjectContent();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            byte[] buffer = new byte[4096];
            while ((len = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, len);
            }
            
            return baos;
//			S3ObjectInputStream inputStream = s3object.getObjectContent();
//			FileUtils.copyInputStreamToFile(inputStream, new File("D:/Users/user/Desktop/hello1.txt"));
		} catch (AmazonServiceException ase) {
			log.error("Caught an AmazonServiceException from GET requests, rejected reasons:");
			log.error("Error Message:    " + ase.getMessage());
			log.error("HTTP Status Code: " + ase.getStatusCode());
			log.error("AWS Error Code:   " + ase.getErrorCode());
			log.error("Error Type:       " + ase.getErrorType());
			log.error("Request ID:       " + ase.getRequestId());
			throw new ObosConnectorException(ase.getStatusCode(), ase.getErrorCode(), ase.getMessage());
		} catch (AmazonClientException | IOException e) {
			log.error("Caught an Exception, which means the client encountered an internal error when downling a file.", e);
			throw new ObosConnectorException(1100, e.getMessage(), e.toString());
		}
	}

	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		
		File convFile = new File(file.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(convFile)) {
			fos.write(file.getBytes());
		}

		return convFile;
	}

	private String generateFileName(MultipartFile multiPart, String filePathName) {
		return filePathName + "/" + multiPart.getOriginalFilename().replace(" ", "_");
	}

	/**
	 * Uploading an object into Nomad S3. We’ll use a putObject() method which accepts three parameters:
	 * bucketName: The bucket name where we want to upload object
	 * key: This is the full path to the file (can contain a folder "folder/file"
	 * file: The actual file containing the data to be uploaded
	 * @param fileName
	 * @param file
	 */
	public void uploadFile(String bucketName, String fileName, File file) throws ObosConnectorException {
		log.debug("Uploading a new object to S3 from a file {} into bucket {}", file.getName(), bucketName);
		try {
			s3client.putObject(
					new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
		} catch (AmazonServiceException ase) {
			log.error("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.",
					ase);
			throw new ObosConnectorException(ase.getStatusCode(), ase.getErrorCode(), ase.getMessage());
		} catch (AmazonClientException ace) {
			log.error("Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3, such as not being able to access the network.",
					ace);
			throw new ObosConnectorException(1000, ace.getMessage(), ace.getStackTrace().toString());
		}
	}

	
	/**
	 * Calling listObjects() method of the s3client object will yield the
	 * ObjectListing object, which can be used to get a list of all the object
	 * summaries in the specified bucket. We’re just printing the key here, but
	 * there are also a couple of other options available, like size, owner, last
	 * modified, storage class, etc…
	 */
	public ObjectListing listObject(String bucketName) throws ObosConnectorException {
		
		ObjectListing objectListing = null;

		try {
			objectListing = s3client.listObjects(bucketName);
		} catch (AmazonServiceException ase) {
			log.error("Caught an AmazonServiceException, which means your request made it to Amazon S3, but was rejected with an error response for some reason.",
					ase);
			throw new ObosConnectorException(ase.getStatusCode(), ase.getErrorCode(), ase.getMessage());
		} catch (AmazonClientException ace) {
			log.error("Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3, such as not being able to access the network.",
					ace);
			throw new ObosConnectorException(1000, ace.getMessage(), ace.getStackTrace().toString());
		}

		return objectListing;
	}
	
	/*
	 *  List all the bucket’s available in nomad S3 environment using the listBuckets() method.
	 *  This method will return a list of all the Buckets
	 * @return List<Bucket>
	 */
	public List<Bucket> listBuckets() {
		return s3client.listBuckets();
	}

	/*
	 * Create the bucket if doesn't exist It’s important to note that the bucket
	 * namespace is shared by all users of the system. So our bucket name must be
	 * unique across all existing bucket names in Amazon S3.
	 * To conform with DNS requirements, the following constraints apply:	 * 
	 * <li>Bucket names should not contain underscores</li>
	 * <li>Bucket names should be between 3 and 63 characters long</li>
	 * <li>Bucket names should not end with a dash 
	 * <li>Bucket names cannot contain adjacent periods Bucket names cannot contain dashes next to periods (e.g., "my-.bucket.com" and "my.-bucket" are invalid)</li> 
	 * <li>Bucket names cannot contain uppercase characters</li>
	 * 
	 */
	public Bucket createBucketName(String bucketName) throws ObosConnectorException {

		log.debug("Start creating S3 bucket with name {}", bucketName);
		Bucket bucketCreated = null;
		if (s3client.doesBucketExist(bucketName)) {
			log.debug("Bucket name is not available Try again with a different Bucket name.");
			throw new ObosConnectorException(1000, "Bucket name " + bucketName + " is not available",
					"Bucket name is not available Try again with a different Bucket name");
		}

		try {
			bucketCreated = s3client.createBucket(bucketName);
		} catch (Exception ace) {
			throw new ObosConnectorException(1000, ace.getMessage(), ace.getStackTrace().toString());
		}

		return bucketCreated;
	}
	
	/*
	 * It’s important to ensure that our bucket is empty before we can delete it. Otherwise, an exception will be thrown
	 * @param bucketName
	 * @return
	 */
	public boolean deleteBucket(String bucketName) throws ObosConnectorException {
		try {
			s3client.deleteBucket(bucketName);
		} catch (AmazonServiceException ase) {
			log.error("Caught an AmazonServiceException, which " + "means your request made it "
					+ "to Amazon S3, but was rejected with an error response" + " for some reason.");
			throw new ObosConnectorException(ase.getStatusCode(), ase.getErrorCode(), ase.getMessage());
		} catch (AmazonClientException e) {
			throw new ObosConnectorException(1000, e.getMessage(), e.getLocalizedMessage());
		}
		return true;
	}
}
