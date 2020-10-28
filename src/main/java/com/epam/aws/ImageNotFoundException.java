package com.epam.aws;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No image found in the bucket")
public class ImageNotFoundException extends RuntimeException {
}
