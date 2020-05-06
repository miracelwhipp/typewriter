package io.github.miracelwhipp.typewriter.freemarker;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfiguration;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Map;

public class DataModel {

    private final Map<String, Object> data;

    private DataModel(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public static DataModel builtIn(MavenProject project, PlexusConfiguration customData) {

        Map<String, Object> input = new HashMap<String, Object>();

        input.put("project", project);

        ZonedDateTime now = ZonedDateTime.now();

        input.put("now", now.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
        input.put("today", now.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        input.put("time", now.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)));
        input.put("timeZone", now.getZone().getId());

        if (customData != null) {

            input.put("dataModel", DataModelParser.parseTemplateModel(customData));
        }

        return new DataModel(input);
    }
}
