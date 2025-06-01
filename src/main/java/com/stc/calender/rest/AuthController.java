package com.stc.calender.rest;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;  

@RestController  
@RequestMapping("/api/auth")  
public class AuthController {  

	@GetMapping("/whoami")  
	public Map<String, Object> whoAmI(@AuthenticationPrincipal Jwt jwt) {  
	  String keycloakId = jwt.getSubject();
	  List<String> roles = jwt.getClaimAsStringList("realm_access"); 
	  String username= jwt.getClaimAsString("preferred_username");
	  String email= jwt.getClaimAsString("email");
	  String name= jwt.getClaimAsString("name");
	  System.out.println(jwt.getClaims());
	  return Map.of("keycloakId", keycloakId, "roles", roles, "username", username, "name", name, "email", email);  
	}    
}  