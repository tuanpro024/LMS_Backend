package com.lms.dictionary.controller;

import com.lms.dictionary.enums.WordType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/word-types")
public class WordTypeController {

    @GetMapping
    public List<Map<String, String>> getAllWordTypes() {
        return Arrays.stream(WordType.values())
                .map(type -> Map.of(
                        "name", type.name(),
                        "value", type.getValue()
                ))
                .collect(Collectors.toList());
    }
}
