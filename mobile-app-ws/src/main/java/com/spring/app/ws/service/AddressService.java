package com.spring.app.ws.service;

import java.util.List;

import com.spring.app.ws.shared.dto.AddressDto;

public interface AddressService {

	List<AddressDto> getAddresses(String id);
	AddressDto getAddress(String addressId);
}
