package com.spring.app.ws.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.jni.Address;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.ws.dao.repository.UserRepository;
import com.spring.app.ws.exceptions.UserServiceException;
import com.spring.app.ws.io.entity.UserEntity;
import com.spring.app.ws.service.UserService;
import com.spring.app.ws.shared.Utils;
import com.spring.app.ws.shared.dto.AddressDto;
import com.spring.app.ws.shared.dto.UserDto;
import com.spring.app.ws.ui.model.response.ErrorMessages;
import com.spring.app.ws.ui.model.response.UserResponse;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	Utils utils;

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

	@Override
	public UserDto createUser(UserDto user) {

		if (userRepository.findByEmail(user.getEmail()) != null)
			throw new RuntimeException("duplicated email ");

		/*
		 * for(AddressDto addres:user.getAddresses()) {
		 *  .collect(Collectors.toList())
		 * addres.setAddressId(utils.generateAddressId(30)); }
		 */
		
		user.getAddresses().forEach(address ->{
			address.setAddressId(utils.generateAddressId(30));
			address.setUserDetails(user);
		});
		
		ModelMapper modelMapper = new ModelMapper();
		UserEntity userEntity = modelMapper.map(user, UserEntity.class);

		String userId = utils.generateUserId(30);
		userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		userEntity.setUserId(userId);

		UserEntity storedUser = userRepository.save(userEntity);

		
		UserDto returnValue =modelMapper.map(storedUser, UserDto.class);
		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public UserDto getUser(String email) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(email);
		if (userEntity == null)
			throw new UsernameNotFoundException(email);
		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException(userId);
		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(userEntity, returnValue);
		return returnValue;
	}

	@Override
	public UserDto updateUser(UserDto user) {
		UserEntity userEntity = userRepository.findByUserId(user.getUserId());
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userEntity.setLastName(user.getLastName());
		userEntity.setFirstName(user.getFirstName());
		UserEntity storedUser = userRepository.save(userEntity);

		UserDto returnValue = new UserDto();
		BeanUtils.copyProperties(storedUser, returnValue);
		return returnValue;
	}

	@Transactional
	@Override
	public void deleteUser(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UserServiceException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

		userRepository.delete(userEntity);

	}

	@Override
	public List<UserDto> getUsers(int page, int limit) {
		List<UserDto> retrunValue = new ArrayList<UserDto>();
		Pageable pageable = PageRequest.of(page, limit);
		Page<UserEntity> userPage = userRepository.findAll(pageable);
		List<UserEntity> userEntities = userPage.getContent();

		for (UserEntity userEntity : userEntities) {
			UserDto userModel = new UserDto();
			BeanUtils.copyProperties(userEntity, userModel);
			retrunValue.add(userModel);
		}
		return retrunValue;
	}

}
