package tr.edu.sakarya.bil002.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Cucumber adımlarının Spring bean'lerine (@Autowired) erişebilmesini sağlar.
 * Bu sınıftan yalnızca bir tane olmalıdır.
 */
@CucumberContextConfiguration
@SpringBootTest
public class CucumberSpringConfiguration {
}
