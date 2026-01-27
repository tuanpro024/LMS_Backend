package com.lms.dictionary.converter;

import com.lms.dictionary.enums.WordType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Converter
public class WordTypeListConverter implements AttributeConverter<List<WordType>, String> {

    private static final String SPLIT_CHAR = ",";

    @Override
    public String convertToDatabaseColumn(List<WordType> wordTypes) {
        if (wordTypes == null || wordTypes.isEmpty()) {
            return null;
        }
        return wordTypes.stream()
                .map(WordType::name)
                .collect(Collectors.joining(SPLIT_CHAR));
    }

    @Override
    public List<WordType> convertToEntityAttribute(String string) {
        if (string == null || string.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(string.split(SPLIT_CHAR))
                .map(s -> {
                    try {
                        return WordType.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
