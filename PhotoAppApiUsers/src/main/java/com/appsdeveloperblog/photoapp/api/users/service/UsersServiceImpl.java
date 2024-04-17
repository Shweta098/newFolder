package com.appsdeveloperblog.photoapp.api.users.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.appsdeveloperblog.photoapp.api.users.shared.UserDto;
import com.appsdeveloperblog.photoapp.api.users.ui.model.AlbumResponseModel;

import feign.FeignException;

import com.appsdeveloperblog.photoapp.api.users.data.*;

@Service
public class UsersServiceImpl implements UsersService {
	
	UsersRepository usersRepository;
	BCryptPasswordEncoder bCryptPasswordEncoder;
	RestTemplate restTemplate;
	AlbumsServiceClient albumsServiceClient;
	Environment env;
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public UsersServiceImpl(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder, 
			RestTemplate restTemplate,
//			AlbumsServiceClient albumsServiceClient,
			Environment env)
	{
		this.usersRepository = usersRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.restTemplate = restTemplate;
//		this.albumsServiceClient=albumsServiceClient;
		this.env = env;
	}
 
	@Override
	public UserDto createUser(UserDto userDetails) {
		// TODO Auto-generated method stub
		
		userDetails.setUserId(UUID.randomUUID().toString());
		userDetails.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));
		
		ModelMapper modelMapper = new ModelMapper(); 
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserEntity userEntity = modelMapper.map(userDetails, UserEntity.class);

		usersRepository.save(userEntity);
		
		UserDto returnValue = modelMapper.map(userEntity, UserDto.class);
 
		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity = usersRepository.findByEmail(username);
		
		if(userEntity == null) throw new UsernameNotFoundException(username);
		
		return new User(userEntity.getEmail(),userEntity.getEncryptedPassword(),
				true, true, true, true, new ArrayList<>());
	}

	@Override
	public UserDto getUserDetailsByEmail(String email) {
		UserEntity userEntity = usersRepository.findByEmail(email);
		
		if(userEntity == null) throw new UsernameNotFoundException(email);
		
		return new ModelMapper().map(userEntity, UserDto.class);
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userEntity = usersRepository.findByUserId(userId);
		if(userEntity == null) throw new UsernameNotFoundException("User not found..!");
		
		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
		//Instead of hardcoding this URL, we will set this in config server, so that other microservices if need to use this URL can access this.
//		String albumsUrl=String.format("http://ALBUMS-WS/users/%s/albums",userId);
		
		//IF we need to use restTemplate:
		
		String albumsUrl=String.format(env.getProperty("albums.url"),userId);
		ResponseEntity<List<AlbumResponseModel>> albumListResp = restTemplate.exchange(albumsUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<AlbumResponseModel>>() {
		});
		List<AlbumResponseModel> albumList = albumListResp.getBody();
		
		
		//IF we need to use feign client :
//		List<AlbumResponseModel> albumList=null;
		//If we use try/catch block to handle feign exception
		/*try {
			albumList = albumsServiceClient.getAlbums(userId);
		} catch (FeignException e) {
			// TODO Auto-generated catch block
			logger.error(e.getLocalizedMessage());
		}*/
		
		//If we use feign error decoder:
		logger.debug("Before calling Albums Microservice");
//		albumList = albumsServiceClient.getAlbums(userId);
		logger.debug("After calling Albums Microservice");
		userDto.setAlbums(albumList);
		
		return userDto;
	}

}
