package com.photoandvision.folder_sharing_pp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class FolderSharingPpApplication {

	public static void main(String[] args) {
		SpringApplication.run(FolderSharingPpApplication.class, args);
	}

}
