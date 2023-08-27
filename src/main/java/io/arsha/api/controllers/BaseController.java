package io.arsha.api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@RestController
public class BaseController {

    @Value("${docs:'https://getpostman.com'}")
    String docs;

    @GetMapping("/")
    public ModelAndView toDocs() {
        log.info("Redirecting to docs: {}", docs);
        return new ModelAndView("redirect:" + docs);
    }
}
