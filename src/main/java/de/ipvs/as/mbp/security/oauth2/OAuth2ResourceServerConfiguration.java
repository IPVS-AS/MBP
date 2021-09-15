//package de.ipvs.as.mbp.security.oauth2;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
//import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
//import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
//import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
//import org.springframework.security.oauth2.provider.token.TokenStore;
//import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
//import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
//
//@Configuration
//@EnableResourceServer
//@PropertySource(value = "classpath:application.properties")
//public class OAuth2ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
//
//	@Value("${security.oauth2.resource.jwt.key-value}")
//	private String signingKey;
//
//	@Override
//	public void configure(ResourceServerSecurityConfigurer config) {
//		config.tokenServices(tokenServices());
//	}
//
//	@Override
//	public void configure(HttpSecurity http) throws Exception {
//		http
//				.authorizeRequests()
//				.antMatchers("/").permitAll();
//	}
//
//	@Bean
//	public TokenStore tokenStore() {
//		return new JwtTokenStore(accessTokenConverter());
//	}
//
//	@Bean
//	public JwtAccessTokenConverter accessTokenConverter() {
//		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
//		converter.setSigningKey(signingKey);
//		return converter;
//	}
//
//	@Bean
//	@Primary
//	public DefaultTokenServices tokenServices() {
//		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
//		defaultTokenServices.setSupportRefreshToken(true);
//		defaultTokenServices.setTokenStore(tokenStore());
//		return defaultTokenServices;
//	}
//}
