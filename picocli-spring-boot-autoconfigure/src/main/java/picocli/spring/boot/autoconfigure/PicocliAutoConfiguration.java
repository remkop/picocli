package picocli.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import picocli.spring.PicocliSpringFactory;

/**
 * @author Thibaud Leprêtre
 */
@Configuration
@ConditionalOnClass(CommandLine.class)
public class PicocliAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IFactory.class)
    public IFactory picocliSpringFactory(ApplicationContext applicationContext) {
        return new PicocliSpringFactory(applicationContext);
    }
}
