package picocli.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import picocli.spring.PicocliSpringFactory;

/**
 * @author Thibaud Leprêtre
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(CommandLine.class)
public class PicocliAutoConfiguration {

    @Primary
    @Bean
    @ConditionalOnMissingBean(IFactory.class)
    public IFactory picocliSpringFactory(ApplicationContext applicationContext) {
        return new PicocliSpringFactory(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean(PicocliSpringFactory.class)
    public PicocliSpringFactory picocliSpringFactoryImpl(ApplicationContext applicationContext) {
        return new PicocliSpringFactory(applicationContext);
    }
}
