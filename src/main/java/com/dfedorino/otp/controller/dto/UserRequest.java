package com.dfedorino.otp.controller.dto;

public record UserRequest(String login, String phoneNumber, String password) {

}
