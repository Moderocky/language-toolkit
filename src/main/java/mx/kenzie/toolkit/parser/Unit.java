package mx.kenzie.toolkit.parser;

public interface Unit extends CharSequence {

    static Unit of(String name) {
        record Simple(String name) implements Unit {

            @Override
            public String toString() {
                return name;
            }

        }
        return new Simple(name);
    }

    @Override
    default int length() {
        return this.name().length();
    }

    @Override
    default char charAt(int index) {
        return this.name().charAt(index);
    }

    @Override
    default CharSequence subSequence(int start, int end) {
        return this.name().subSequence(start, end);
    }

    String name();

}
