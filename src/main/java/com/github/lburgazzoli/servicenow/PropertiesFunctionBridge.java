package com.github.lburgazzoli.servicenow;

import io.fabric8.karaf.core.properties.function.PropertiesFunction;

public class PropertiesFunctionBridge implements PropertiesFunction {

    private final io.fabric8.karaf.core.properties.function.PropertiesFunction function;

    public PropertiesFunctionBridge(io.fabric8.karaf.core.properties.function.PropertiesFunction function) {
        this.function = function;
    }

    @Override
    public String getName() {
        return function.getName();
    }

    @Override
    public String apply(String remainder) {
        return function.apply(remainder);
    }
}
