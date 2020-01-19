package com.spring.app.ws.ui.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.app.ws.exceptions.UserServiceException;
import com.spring.app.ws.service.AddressService;
import com.spring.app.ws.service.UserService;
import com.spring.app.ws.shared.dto.AddressDto;
import com.spring.app.ws.shared.dto.UserDto;
import com.spring.app.ws.ui.model.request.UserDetailsRequestModel;
import com.spring.app.ws.ui.model.response.AddressResponse;
import com.spring.app.ws.ui.model.response.ErrorMessages;
import com.spring.app.ws.ui.model.response.OperationStatusModel;
import com.spring.app.ws.ui.model.response.RequestOperationStatus;
import com.spring.app.ws.ui.model.response.UserResponse;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	UserService userService;
	
	@Autowired
	AddressService addressService;

	@GetMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserResponse getUser(@PathVariable String id) {
		UserResponse userResponse = new UserResponse();
		UserDto userDto = userService.getUserByUserId(id);
		BeanUtils.copyProperties(userDto, userResponse);
		return userResponse;
	}

	@PostMapping(consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }, produces = {
			MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public UserResponse createUser(@RequestBody UserDetailsRequestModel userDetails) {

		if (userDetails.getEmail() == null)
			throw new UserServiceException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());
		
		ModelMapper modelMapper = new ModelMapper();
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);
		
		UserDto createdUser = userService.createUser(userDto);
		UserResponse userResponse =modelMapper.map(createdUser, UserResponse.class);
		return userResponse;
	}

	@PutMapping(path = "/{id}", consumes = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_XML_VALUE,
					MediaType.APPLICATION_JSON_VALUE })
	public UserResponse updateUser(@PathVariable String id, @RequestBody UserDetailsRequestModel userDetails) {
		UserResponse userResponse = new UserResponse();

		UserDto userDto = new UserDto();
		BeanUtils.copyProperties(userDetails, userDto);
		userDto.setUserId(id);

		UserDto updatedUser = userService.updateUser(userDto);

		BeanUtils.copyProperties(updatedUser, userResponse);
		return userResponse;
	}

	@DeleteMapping(path = "/{id}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public OperationStatusModel deleteUser(@PathVariable String id) {

		OperationStatusModel operationStatusModel = new OperationStatusModel();
		operationStatusModel.setOperationName(RequestOperationStatus.DELETE.name());

		userService.deleteUser(id);

		operationStatusModel.setOperationResult(RequestOperationStatus.SUCCESS.name());
		return operationStatusModel;
	}

	@GetMapping(produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	public List<UserResponse> getUsers(@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "limit", defaultValue = "2") int limit) {
		List<UserResponse> returnValue = new ArrayList<>();

		List<UserDto> users = userService.getUsers(page, limit);

		/*
		 * Type listType = new TypeToken<List<UserResponse>>() { }.getType();
		 * returnValue = new ModelMapper().map(users, listType);
		 */

		for (UserDto userDto : users) {
			UserResponse userModel = new UserResponse();
			BeanUtils.copyProperties(userDto, userModel);
			returnValue.add(userModel);
		}

		return returnValue;
	}
	
	@GetMapping(path = "/{id}/addresses",produces = {
			MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE,
			"application/hal+json"
			})
	public CollectionModel<AddressResponse> getUserAddresses(@PathVariable String id){
		
		List<AddressResponse> returnValue=new ArrayList<AddressResponse>();
		
		List<AddressDto> addresses=addressService.getAddresses(id);
		
		if(addresses !=null && !addresses.isEmpty()) {
			Type listType = new TypeToken<List<AddressResponse>>() {}.getType();
			returnValue = new ModelMapper().map(addresses, listType);
			for(AddressResponse addressResponse:returnValue) {
				Link userLink=linkTo(methodOn(UserController.class).getUser(id)).withRel("user");
				Link addressLink=linkTo(methodOn(UserController.class).getUserAddress(id, addressResponse.getAddressId())).
						withSelfRel();
				addressResponse.add(addressLink);
				addressResponse.add(userLink);
			}
		}
		
		
		return new CollectionModel<>(returnValue);
	}
	
	@GetMapping(path = "/{id}/addresses/{addressId}",produces = { 
			MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_VALUE,
			"application/hal+json"
			})
	public EntityModel<AddressResponse> getUserAddress(@PathVariable String id,@PathVariable String addressId){
		
		
		AddressDto address=addressService.getAddress(addressId);
		
		Link addressLink=linkTo(methodOn(UserController.class).getUserAddress(id, addressId)).withSelfRel();
		
		Link userLink=linkTo(methodOn(UserController.class).getUser(id)).withRel("user");
		Link addressesLink=linkTo(methodOn(UserController.class).getUserAddresses(id)).withRel("addresses");
		
		AddressResponse returnValue=new ModelMapper().map(address, AddressResponse.class);
		returnValue.add(addressLink);
		returnValue.add(userLink);
		returnValue.add(addressesLink);
		
		return new EntityModel<>(returnValue);
	}

}
