package tr.edu.sakarya.bil002.bdd;

import static io.cucumber.junit.platform.engine.Constants.FEATURES_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.Suite;

/**
 * Cucumber senaryolarını JUnit 5 üzerinden çalıştırır.
 * Feature dosyaları: src/test/resources/features
 * Adım tanımları (step definitions): bu paket (tr.edu.sakarya.bil002.bdd)
 *
 * Kasıtlı bir hatayı yakalayan senaryoları @hata etiketiyle işaretleyin (bkz. docs/ODEV_REHBERI.md).
 * failIfNoTests = false: CI'da yalnızca @hata senaryoları çalıştırılırken hiç senaryo yoksa hata vermez.
 */
@Suite(failIfNoTests = false)
@IncludeEngines("cucumber")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "tr.edu.sakarya.bil002.bdd")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
public class CucumberRunnerTest {
}
