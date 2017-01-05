public class Palindrome {

	public static Deque<Character> wordToDeque(String word) {
		ArrayDeque<Character> d = new ArrayDeque<Character> ();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            d.addLast(c);
        }
        return d;
    }

	public static boolean isPalindrome(String word) {
        Deque<Character> d = wordToDeque(word);
        return pal(d, new Equals());

    }

    public static boolean isPalindrome(String word, CharacterComparator cc) {
        Deque<Character> d = wordToDeque(word);
        return pal(d, cc);

    }

    private static boolean pal(Deque<Character> d, CharacterComparator c) {
        if (d.size() == 0 || d.size() == 1) {
            return true;
        }
        else if (!c.equalChars((char) d.get(0), (char)d.get(d.size()-1))) {
            return false;
        }
        else {
            Deque<Character> temp = d;
            temp.removeFirst();
            temp.removeLast();
            return pal(temp, c);
        }
    }

}