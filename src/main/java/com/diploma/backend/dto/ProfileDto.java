package com.diploma.backend.dto;

import lombok.Data;
import java.util.*;

@Data
public class ProfileDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
}
