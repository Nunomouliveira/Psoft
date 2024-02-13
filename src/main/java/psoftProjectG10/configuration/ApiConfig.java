package psoftProjectG10.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import psoftProjectG10.deviceManagement.services.FileStorageProperties;

@Configuration
@EnableConfigurationProperties({ FileStorageProperties.class })
public class ApiConfig {

	/*
	 * Etags
	 */

	/*
	 * @Bean public FilterRegistrationBean<ShallowEtagHeaderFilter>
	 * shallowEtagHeaderFilter() { FilterRegistrationBean<ShallowEtagHeaderFilter>
	 * filterRegistrationBean = new FilterRegistrationBean<>( new
	 * ShallowEtagHeaderFilter()); filterRegistrationBean.addUrlPatterns("/foos/*");
	 * filterRegistrationBean.setName("etagFilter"); return filterRegistrationBean;
	 * }
	 */

	// We can also just declare the filter directly
	@Bean
	public ShallowEtagHeaderFilter shallowEtagHeaderFilter() {
		return new ShallowEtagHeaderFilter();
	}

	/*
	 * OpenAPI
	 */
	@Bean
	public OpenAPI openApi() {
		return new OpenAPI().info(new Info().title("ACME API").description("ACME API").version("v1.0")
				.contact(new Contact().name("David Rodrigues").email("1211574@isep.ipp.pt")).termsOfService("TOC")
				.license(new License().name("MIT").url("#")));
	}
}
