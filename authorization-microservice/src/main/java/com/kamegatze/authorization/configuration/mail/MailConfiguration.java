package com.kamegatze.authorization.configuration.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
public class MailConfiguration {
    @Value("${mail.path}")
    private String pathMail;
    @Value("${mail.suffix}")
    private String suffix;
    @Value("${mail.template-mode}")
    private String templateMode;
    @Value("${mail.encoding}")
    private String encoding;
    @Bean
    public ITemplateResolver thymeleafTemplateResolver() {
        FileTemplateResolver templateResolver = new FileTemplateResolver();
        templateResolver.setPrefix(pathMail);
        templateResolver.setSuffix(suffix);
        templateResolver.setTemplateMode(templateMode);
        templateResolver.setCharacterEncoding(encoding);
        return templateResolver;
    }
    @Bean
    public SpringTemplateEngine defaultTemplateResolver(ITemplateResolver templateResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        return templateEngine;
    }
}
