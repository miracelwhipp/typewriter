package io.github.miracelwhipp.typewriter;

import io.github.miracelwhipp.typewriter.spi.Monitor;

import java.io.File;
import java.util.*;

public class SensorRegistry {

    private final Map<String, List<Monitor>> sensors = new HashMap<>();

    public static final SensorRegistry INSTANCE = new SensorRegistry();

    private SensorRegistry() {
    }

    public void register(File filename, Monitor sensor) {

        sensors.computeIfAbsent(filename.getAbsolutePath(), name -> new ArrayList<Monitor>()).add(sensor);
    }

    public List<Monitor> getSensors(File filename) {

        return sensors.computeIfAbsent(filename.getAbsolutePath(), x -> Collections.emptyList());
    }
}
