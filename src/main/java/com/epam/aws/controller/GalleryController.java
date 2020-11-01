package com.epam.aws.controller;

import com.epam.aws.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@RestController
public class GalleryController {

    @Autowired
    private ImageService storageService;

    @GetMapping("/api/images/{name}")
    public ModelAndView getImageByName(@PathVariable("name") String name) {
        return new ModelAndView("redirect:" + storageService.getImageByName(name).toString());
    }

    @GetMapping("/api/images/random")
    public ModelAndView getRandomImage() {
        if (storageService.getRandomImage() != null) {
            return new ModelAndView("redirect:" + storageService.getRandomImage().toString());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No image has been uploaded to the bucket");
        }
    }

    @PostMapping("/api/images")
    public ModelAndView uploadImage(@RequestParam("file") MultipartFile file,
                                    RedirectAttributes redirectAttributes) throws IOException {
        storageService.uploadFileToS3(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return new ModelAndView("redirect:/", HttpStatus.CREATED);
    }

}
