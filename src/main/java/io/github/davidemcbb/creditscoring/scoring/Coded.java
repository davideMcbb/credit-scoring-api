package io.github.davidemcbb.creditscoring.scoring;

/** A categorical input with the numeric code used in the South German Credit data. */
public interface Coded {

    int code();

    static <E extends Enum<E> & Coded> E fromCode(Class<E> type, int code) {
        for (E value : type.getEnumConstants()) {
            if (value.code() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("No " + type.getSimpleName() + " with code " + code);
    }
}
