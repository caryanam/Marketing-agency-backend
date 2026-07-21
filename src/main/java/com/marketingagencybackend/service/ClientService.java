package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.ClientCreateRequestDTO;
import com.marketingagencybackend.dto.ClientResponseDTO;
import com.marketingagencybackend.dto.ClientUpdateRequestDTO;

import java.util.List;

public interface ClientService {

    ClientResponseDTO createClient(ClientCreateRequestDTO request);

    ClientResponseDTO getClientById(Long id);

    List<ClientResponseDTO> getAllClients();

    ClientResponseDTO updateClient(Long id, ClientUpdateRequestDTO request);

    void deleteClient(Long id);
}
