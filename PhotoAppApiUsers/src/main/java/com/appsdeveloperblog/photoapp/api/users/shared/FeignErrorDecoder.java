package com.appsdeveloperblog.photoapp.api.users.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import feign.Response;
import feign.codec.ErrorDecoder;

@Component
public class FeignErrorDecoder implements ErrorDecoder{
	
	@Autowired
	private Environment env;

	@Override
	public Exception decode(String methodKey, Response response) {
		switch(response.status()) {
		case 400:
			//return new BadRequestException
			break;
		case 404:
		{
			//can create a custom exception
			if(methodKey.contains("getAlbums"))
				return new ResponseStatusException(HttpStatus.valueOf(response.status()),
						env.getProperty("albums.exception.albums-not-found"));
			break;
		}
		default:
			return new Exception(response.reason());
		}
		return null;
	}

}
