package com.linkedin.userservice.dto;

import com.linkedin.userservice.entity.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message ="Invalid email format")
    private String email;

    @NotBlank(message = "password is required")
    @Size(min = 6 , message = "password must be atleast 6 characters")
    private String password;

    @NotBlank(message = "first name required")
    private String firstName;

    @NotBlank(message = "last name required")
    private String lastName;

    private String headline;

    private String location;


}
