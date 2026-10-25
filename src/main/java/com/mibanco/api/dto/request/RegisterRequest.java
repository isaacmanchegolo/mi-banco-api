package com.mibanco.api.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
// Pendiente importaciones
@Getter
@Setter
public class RegisterRequest {
    @NotBlank(message="El nombre no puede estar vacío")
    @Size(max=50, message="El nombre no puede superar 50 caracteres")
    private String firstName;

    @NotBlank(message="El apellido no puede estar vacío")
    @Size(max=50, message="El apellido no puede superar 50 caracteres")
    private String lastName;

    @Email(message="Debe proporcionar un correo valido")
    @NotBlank(message="El correo no puede estar vacío")
    @Size(max=80, message="El correo no puede superar 80 caracteres")
    private String email;

    @Size(min=6, message = "El password  debe tener al menos 6 caracteres")
    @NotBlank(message="El password no puede estar vacío")
    private String password;
}
