package codeForZk;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class testStream {

	String str = "a";
	static Consumer<String> printString = System.out::println;
	static Consumer<Apple> printApple = System.out::println;

	class Apple implements Comparable {
		public static final String BigApple = "big apple";
		private int size;
		private String tag;
		@Override
		public int compareTo(Object o) {
			if(o instanceof Apple)
			return this.size - ((Apple)o).size;
			return -1;
		}
		public Apple(int size) {
			this.size = size;
		}

		public int getSize() {
			return size;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}

		@Override
		public String toString() {
			return size + "" + (tag == null ? "" : tag);
		}

	}

	public void action() {
		List<String> myList = Arrays.asList("a1", "a2", "b1", "c2", "c1");

		myList.stream().filter(s -> s.startsWith(str)).map(String::toUpperCase).sorted().forEach(printString);
		System.out.println("++++++");
		Stream<String> test = Stream.of("a1", "a2", "b1", "c2", "c1");
		test.filter(s -> s.endsWith("1")).sorted().forEach(printString);

	}

	public void filterApple() {
		Supplier<Stream<Apple>> streamSupplier = () -> Stream.of(this.new Apple(1), this.new Apple(3),
				this.new Apple(2));
		streamSupplier.get().sorted().forEach(printApple);
		List<Apple> newSet = streamSupplier.get().filter(a -> a.getSize() > 1).peek(a -> a.setTag(Apple.BigApple))
				.collect(Collectors.toList());
		;
		newSet.stream().map(a -> a.toString()).forEach(printString);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		new testStream().action();
		new testStream().filterApple();
	}

}
