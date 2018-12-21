package com.mstools.connector.obos;

import java.io.File;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mstools.connector.obos.RestConnectorOBOSApplication;
import com.mstools.connector.obos.exception.ObosConnectorException;
import com.mstools.connector.obos.service.AmazonClient;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = RestConnectorOBOSApplication.class)
@Slf4j
public class ObosConnector {

	@Autowired
	private AmazonClient amazonClient;
	
	@Test
	public void listBuckets() {
		List<Bucket> buckets = amazonClient.listBuckets();
		for (Bucket bucket : buckets) {
			log.debug("Bucket name : {}", bucket.getName());
		}
	}
	
	@Test
	public void createBuckets() {
		try {
			amazonClient.createBucketName("hbf-bucket");
		} catch (ObosConnectorException e) {
			log.error("Error Message:    " + e.getMessage());
			log.error("HTTP Status Code: " + e.getCode());
			log.error("HTTP Description: " + e.getDescription());
		}
	}
	
	@Test
	public void deleteBuckets() {
		try {
			amazonClient.deleteBucket("hbf-bucket");
		} catch (ObosConnectorException e) {
			log.error("Error Message:    " + e.getMessage());
			log.error("HTTP Status Code: " + e.getCode());
			log.error("HTTP Description: " + e.getDescription());
		}
	}
	
	@Test
	public void putObject() {
		try {
			amazonClient.uploadFile("your-bucket-name11", "folder/folder//SendReportServiceTest.scala", new File(
					"D:\\Projects\\bulksms\\bulksms\\messaging-pro-api\\src\\test\\java\\com\\orange\\olpstn\\bulk\\api\\test\\unit\\SendReportServiceTest.java"));
		} catch (ObosConnectorException e) {
			log.error("Error Message:    " + e.getMessage());
			log.error("HTTP Status Code: " + e.getCode());
			log.error("HTTP Description: " + e.getDescription());
		}
	}
	
	@Test
	public void listObject() {
		try {
			ObjectListing objectListing = amazonClient.listObject("dummynomad");
			for (S3ObjectSummary os : objectListing.getObjectSummaries()) {
				log.info(os.getKey());
			}
		} catch (ObosConnectorException e) {
			log.error("Error Message:    " + e.getMessage());
			log.error("HTTP Status Code: " + e.getCode());
			log.error("HTTP Description: " + e.getDescription());
		}
	}
	
	@Test
	public void downloadObject() {
		try {
			amazonClient.downloadFile("dummynomad", "xxxx/Penguins.jpg");
		} catch (ObosConnectorException e) {
			log.error("Error Message:    " + e.getMessage());
			log.error("HTTP Status Code: " + e.getCode());
			log.error("HTTP Description: " + e.getDescription());
		}
	}
	
	@Test
	public void deleteObject() {
		try {
			String result = amazonClient.deleteFileFromS3Bucket("dummynomad", "1542047383759-C:\\wamp64\\www\\g4\\contracts\\2018050817220495\\C10000018\\18_1542047371752_img.jpeg");
			log.debug("*******result : " + result);
		} catch (ObosConnectorException e) {
			log.error("Error Message:    " + e.getMessage());
			log.error("HTTP Status Code: " + e.getCode());
			log.error("HTTP Description: " + e.getDescription());
		}
	}
	
}
