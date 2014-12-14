package au.com.madman.oauth2_provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * From  https://github.com/dsyer/sparklr-boot
 *
 * Here are some curl commands to use to get started:

    $ curl -H " Accept: application/json" my-client-with-secret:secret@localhost:8080/oauth/token -d grant_type=client_credentials
    {... "access_token": "b561ff06-4259-466e-92d8-781db1a51901", ...}
    $ TOKEN=b561ff06-4259-466e-92d8-781db1a5190
    $ curl -H "Authorization: Bearer $TOKEN" localhost:8080/
    Hello World
    
 * NOTE:
 * (1) For access token URL, localhost:8080/oauth/token
 *  (1a) need to add "AUthorisation" header: Base <my-client-with-secret:secret (Base 64), i.e "Basic bXktY2xpZW50LXdpdGgtc2VjcmV0OnNlY3JldA=="
 *  (1b) need to set content type to "application/x-www-form-urlencoded" or will get "Missing grant type" error
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@RestController
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    

    @RequestMapping("/")
    public String home() {
        return "Hello World";
    }

    @Configuration
    @EnableResourceServer
    protected static class ResourceServer extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http
                // Just for laughs, apply OAuth protection to only 2 resources
                .requestMatchers().antMatchers("/","/admin/beans").and()
                .authorizeRequests()
                .anyRequest().access("#oauth2.hasScope('read')");
            // @formatter:on
        }

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.resourceId("sparklr");
        }

    }

    @Configuration
    @EnableAuthorizationServer
    protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.authenticationManager(authenticationManager);
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            // @formatter:off
            clients.inMemory()
                .withClient("my-trusted-client")
                    .authorizedGrantTypes("password", "authorization_code", "refresh_token", "implicit")
                    .authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
                    .scopes("read", "write", "trust")
                    .resourceIds("sparklr")
                    .accessTokenValiditySeconds(60)
            .and()
                .withClient("my-client-with-registered-redirect")
                    .authorizedGrantTypes("authorization_code")
                    .authorities("ROLE_CLIENT")
                    .scopes("read", "trust")
                    .resourceIds("sparklr")
                    .redirectUris("http://anywhere?key=value")
            .and()
                .withClient("my-client-with-secret")
                    .authorizedGrantTypes("client_credentials", "password")
                    .authorities("ROLE_CLIENT")
                    .scopes("read")
                    .resourceIds("sparklr")
                    .secret("secret");
        // @formatter:on
        }

    }
}
