import java.util.*;

public class Word
{
   protected String word;
   protected int freq;

   Word(String w, int f)
   {
      word = w;
      freq = f;
   }

   public String getWord() { return word; }
   public int getFreq() { return freq; }
   public String toString() { return word + " " + freq; }
}

class ByWord implements Comparator
{
   public int compare(Object o1, Object o2)
   {
      String s1 = ((Word)o1).getWord();
      String s2 = ((Word)o2).getWord();
      return s1.compareTo(s2);
   }
}

class ByFreq implements Comparator
{
   public int compare(Object o1, Object o2)
   {
      double d1 = ((Word)o1).getFreq();
      double d2 = ((Word)o2).getFreq();
      return (int)(d2 - d1);
   }
}
