package com.kamegatze.authorization.advice;

import com.kamegatze.authorization.controllers.AuthenticationController;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice(assignableTypes = AuthenticationController.class)
public class AuthenticationAdvice {


}
