package com.spring.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.app.ws.dao.repository.AddressRepository;
import com.spring.app.ws.dao.repository.UserRepository;
import com.spring.app.ws.io.entity.AddressEntity;
import com.spring.app.ws.io.entity.UserEntity;
import com.spring.app.ws.service.AddressService;
import com.spring.app.ws.shared.dto.AddressDto;

@Service
public class AddressServiceImpl implements AddressService {
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	AddressRepository addressRepository;

	@Override
	public List<AddressDto> getAddresses(String id) {
		List<AddressDto> returnValue=new ArrayList<>();
		
		UserEntity userEntity=userRepository.findByUserId(id);
		if(userEntity== null)
			return returnValue;
		
		Iterable<AddressEntity> addresses = addressRepository.findAllByUserDetails(userEntity);
		
		addresses.forEach(address ->{
			returnValue.add(new ModelMapper().map(address, AddressDto.class));
		});
		return returnValue;
	}

	@Override
	public AddressDto getAddress(String addressId) {
		
		AddressEntity addressEntity=addressRepository.findByAddressId(addressId);
		AddressDto addressDto=new ModelMapper().map(addressEntity, AddressDto.class);
		return addressDto;
	}

}
