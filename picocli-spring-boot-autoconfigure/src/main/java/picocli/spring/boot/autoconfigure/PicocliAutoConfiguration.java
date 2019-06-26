package picocli.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import picocli.CommandLine;

/**
 * @author Thibaud Leprêtre
 */
@Configuration
@ConditionalOnClass(CommandLine.class)
public class PicocliAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CommandLine.IFactory.class)
    CommandLine.IFactory springPicocliFactory(ApplicationContext applicationContext) {
        return new SpringPicocliFactory(applicationContext);
    }


}
