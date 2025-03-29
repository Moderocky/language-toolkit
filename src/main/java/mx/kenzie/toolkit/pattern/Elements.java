package mx.kenzie.toolkit.pattern;

interface Elements extends CharSequence {

    @Override
    default int length() {
        int amount = elements().length - 1;
        for (CharSequence element : elements()) amount += element.length();
        return amount;
    }

    @Override
    default char charAt(int index) {
        return this.toString().charAt(index);
    }

    @Override
    default CharSequence subSequence(int start, int end) {
        return this.toString().subSequence(start, end);
    }

    CharSequence[] elements();

}
