package com.lms.dictionary.enums;

public enum WordType {
    NOUN("Danh từ"),
    VERB("Động từ"),
    ADJECTIVE("Tính từ"),
    ADVERB("Trạng từ"),
    PRONOUN("Đại từ"),
    PREPOSITION("Giới từ"),
    CONJUNCTION("Liên từ"),
    INTERJECTION("Thán từ"),
    IDIOM("Thành ngữ"),
    PHRASE("Cụm từ"),
    PARTICLE("Trợ từ"),
    AUXILIARY_VERB("Trợ động từ"),
    MEASURE_WORD("Lượng từ"),
    NUMERAL("Số từ"),
    ONOMATOPOEIA("Từ tượng thanh"),
    PREFIX("Tiền tố"),
    SUFFIX("Hậu tố"),
    ADJECTIVAL_VERB("Hình dung từ");

    private final String value;

    WordType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public static WordType fromValue(String value) {
        for (WordType type : WordType.values()) {
            if (type.value.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown WordType: " + value);
    }
}
