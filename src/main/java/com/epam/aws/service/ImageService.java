package com.epam.aws.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.epam.aws.dto.Image;
import com.epam.aws.repository.ImageRepository;
import com.epam.aws.util.AwsUtils;
import com.epam.aws.util.FileUtils;
import com.epam.aws.util.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ImageService {

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ImageRepository repository;

    @Autowired
    private AmazonS3 s3Client;

    @Value("${amazon.s3.bucket-name}")
    private String bucketName;

    public void uploadFileToS3(MultipartFile file) throws IOException {
        try {
            TransferManager tm = TransferManagerBuilder.standard()
                                                       .withS3Client(s3Client)
                                                       .build();
            File fileFromMultipart = FileUtils.convertMultipartToFile(file);
            String normalizedFilename = fileFromMultipart.getName().replace(" ", "_");
            Upload upload = tm.upload(new PutObjectRequest(bucketName,
                    normalizedFilename,
                    fileFromMultipart)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            upload.waitForCompletion();
            Image image = Image
                    .builder()
                    .imageLocation(new URL(((AmazonS3Client) s3Client)
                            .getResourceUrl(bucketName, normalizedFilename)))
                    .dateCreated(LocalDateTime.now())
                    .imageName(normalizedFilename)
                    .build();
            repository.save(image);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public URL getImageByName(String name) {
        repository.findByImageName(name);
        return s3Client.generatePresignedUrl(AwsUtils.getPresignedUrlRequest(bucketName, name));
    }

    public URL getRandomImage() {
        List<Image> savedImages = repository.findAll();

        Image randomImage = null;
        if (savedImages.size() > 0) {
            randomImage = savedImages.get(RandomUtils.getRandomInt(savedImages.size() - 1));
            return s3Client.generatePresignedUrl(AwsUtils.getPresignedUrlRequest(bucketName,
                    randomImage.getImageName()));
        }
        return null;

    }
}
