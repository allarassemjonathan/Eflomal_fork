public class SentencePair {
    private final Sentence first;
    private final Sentence second;
    public SentencePair(Sentence first, Sentence second) {
        this.first = first;
        this.second = second;
    }

    public Sentence getFirst() {
        return first;
    }

    public Sentence getSource() {
        return first;
    }

    public Sentence getTarget() {
        return second;
    }

    public Sentence getSecond() {
        return second;
    }

    public Sentence get(int i) {
        if (i == 0) {
            return first;
        } else if (i == 1) {
            return second;
        } else {
            throw new IndexOutOfBoundsException(String.format("Sentence pairs do not support index: %d", i));
        }
    }
}
