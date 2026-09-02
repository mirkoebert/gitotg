package com.mirkoebert.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.mirkoebert.cucumber")
// html goes straight into the site output dir so mvn test site publishes it;
// like the JaCoCo and surefire reports it is simply absent if the tests did not run
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/site/cucumber.html")
public class CucumberTest {
}
