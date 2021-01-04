package io.github.miracelwhipp.typewriter.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConversionDescription {

    private final List<String> conversions;
    private String conversionKey;

    public ConversionDescription(List<String> conversions) {
        this.conversions = conversions;
    }

    public List<String> getConversions() {
        return conversions;
    }

    public String getConversionKey() {

        if (conversionKey == null) {

            conversionKey = String.join(".", conversions);
        }

        return conversionKey;
    }

    public ConversionDescription append(ConversionDescription suffix) {


        ArrayList<String> conversions = new ArrayList<>();

        conversions.addAll(this.conversions);
        conversions.addAll(suffix.conversions);

        ConversionDescription result = new ConversionDescription(conversions);

        if (this.conversionKey != null && suffix.conversionKey != null) {

            result.conversionKey = this.conversionKey + "." + suffix.conversionKey;
        }

        return result;
    }

    public String getFinalExtension() {

        return conversions.get(conversions.size() - 1);
    }


    public static ConversionDescription of(List<String> conversions) {

        return new ConversionDescription(conversions);
    }

    public static ConversionDescription of(String... conversions) {

        return of(Arrays.asList(conversions));
    }

}
