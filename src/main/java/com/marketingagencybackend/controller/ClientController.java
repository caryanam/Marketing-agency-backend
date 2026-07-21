package com.marketingagencybackend.controller;


import com.marketingagencybackend.dto.ApiResponseDTO;
import com.marketingagencybackend.dto.ClientCreateRequestDTO;
import com.marketingagencybackend.dto.ClientResponseDTO;
import com.marketingagencybackend.dto.ClientUpdateRequestDTO;
import com.marketingagencybackend.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    //Client Registration
    @PostMapping("/registration")
    public ResponseEntity<ApiResponseDTO<ClientResponseDTO>> createClient(
            @Valid @RequestBody ClientCreateRequestDTO request) {

        ClientResponseDTO response = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDTO<>(
                        "SUCCESS",
                        "Client created successfully.",
                        response
                ));
    }


    //Get Client By Id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ClientResponseDTO>> getClientById(
            @PathVariable Long id) {

        ClientResponseDTO response = clientService.getClientById(id);

        return ResponseEntity.ok(
                new ApiResponseDTO<>(
                        "SUCCESS",
                        "Client fetched successfully.",
                        response
                )
        );
    }


    //Get All Clients
    @GetMapping("/all")
    public ResponseEntity<ApiResponseDTO<List<ClientResponseDTO>>> getAllClients() {

        List<ClientResponseDTO> response = clientService.getAllClients();

        return ResponseEntity.ok(
                new ApiResponseDTO<>(
                        "SUCCESS",
                        "Clients fetched successfully.",
                        response
                )
        );
    }


     //Update Client Info
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponseDTO<ClientResponseDTO>> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientUpdateRequestDTO request) {

        ClientResponseDTO response =
                clientService.updateClient(id, request);

        return ResponseEntity.ok(
                new ApiResponseDTO<>(
                        "SUCCESS",
                        "Client updated successfully.",
                        response
                )
        );
    }


     //Delete Client
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponseDTO<Object>> deleteClient( @PathVariable Long id) {

        clientService.deleteClient(id);
        return ResponseEntity.ok(
                new ApiResponseDTO<>(
                        "SUCCESS",
                        "Client deleted successfully.",
                        null
                )
        );
    }

}
