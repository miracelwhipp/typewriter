package io.github.miracelwhipp.typewriter.spi;

public interface Monitor {

    Object newSensor();

    ElementConverter newEvaluator();
}
