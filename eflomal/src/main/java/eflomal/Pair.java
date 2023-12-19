package eflomal;

import java.util.TreeMap;

public class Pair implements Comparable {

	private TreeMap<Integer, Integer> content;
	private Integer key;
	private Integer value;

	public Pair() {

	}

	public Pair(Integer v1, Integer v2) {
		this.content = new TreeMap<>();
		this.content.put(v1, v2);
		this.key = v1;
		this.value = v2;
	}

	public Integer getKey() {
		return key;
	}

	public Integer getValue() {
		return value;
	}

	public int compareto(Pair p) {
		if (p.getKey() == this.getKey() && p.getValue() == this.getValue()) {
			return 1;
		}
		return 0;
	}

	public String toString() {
		return "(" + this.getKey() + ", " + this.getValue() + ")";
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		try {
			return compareto((Pair) o);
		} catch (Exception e) {
			System.out.print("Wrong type!");
		}
		return 0;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
