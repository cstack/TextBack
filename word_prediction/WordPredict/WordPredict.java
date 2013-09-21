/*****************************************************************
** WordPredict - word prediction demo program
**
** Usage: java WordPredict dictionary max_choices
**
** where 'dictionary' is a file containing words and word
** frequencies, for example...
**
**    the 5776384
**    of 2789403
**    and 2421302
**    a 1939617
**    in 1695860
**    ...
**
** and 'max_choices' is the maximum number of choices to display
** for a given word stem
**
** (c) Scott MacKenzie, 2000                             
******************************************************************/
import java.io.*;
import java.util.*;

public class WordPredict
{
   public static void main(String[] args) throws IOException
   {
      // exactly two command line arguments needed
      if (args.length != 2)
      {
         System.out.println("usage: java WordPredict dictionary max_choices");
         return;
      }

      // ensure dictionary file exists
      File f = new File(args[0]);
      if (!f.exists())
      {
         System.out.println("File not found - " + args[0]);
         return;
      }

      // get maximum number of choices to display
      int max = Integer.parseInt(args[1]);

      // open dictionary file for input
      BufferedReader inputFile =
         new BufferedReader(new FileReader(args[0]));

      // open stdin for keyboard input
      BufferedReader stdin =
         new BufferedReader(new InputStreamReader(System.in), 1);
     
      // read lines from dictionary ('word' and 'frequency' on each)
      String line;
      Vector v = new Vector();
      System.out.print("Loading dictionary... ");
      while ((line = inputFile.readLine()) != null)
      {
         StringTokenizer st = new StringTokenizer(line);

         // exact two entries per line required
         if (st.countTokens() != 2)
         {
            System.out.println("Dictionary format error");
            return;
         }

         // get the word
         String newWord = st.nextToken();

         // get the frequency
         int newFreq = Integer.parseInt(st.nextToken());

         // add to vector as a Word object
         v.addElement(new Word(newWord, newFreq));
      }

      // close disk file
      inputFile.close(); 

      // declare a Word array of just the size needed
      Word[] w = new Word[v.size()];

      // copy elements from vector into array
      v.copyInto(w);

      System.out.println("(done)");
      System.out.println("Dictionary contains " + w.length + " words");

      // sort the dictionary by word
      System.out.print("Sorting dictionary... ");
      Arrays.sort(w, new ByWord());
      System.out.println("(done)");

      // let the user play with the program...
      String s1;
      System.out.println("Enter word or word stem...");
      while ((s1 = stdin.readLine()) != null)
      {
         // s2 is 1st entry 'after' entries beginning with s1 stem
         String s2 = s1.substring(0, s1.length() - 1)
             + (char)(s1.charAt(s1.length() - 1) + 1);

         // find position in array of word entered and s2
         int n1 = Arrays.binarySearch(w, new Word(s1, 0), new ByWord());
         int n2 = Arrays.binarySearch(w, new Word(s2, 0), new ByWord());

         // if either doesn't exit, that's OK, but use +ve index anyway
         if (n1 < 0) n1 = -n1 - 1;
         if (n2 < 0) n2 = -n2 - 1;

         // proceed only if there are some choices for the entered word
         if (n2 - n1 > 0)
         {
            // make a new Word array for all choices with s1 stem
            Word[] w2 = new Word[n2 - n1];
            int i, j;
            for (i = n1, j = 0; i < n2; i++, j++)
               w2[j] = w[i];

            // sort the new array by frequency
            Arrays.sort(w2, new ByFreq());

            // output the most frequent words matching stem
            int n = w2.length > max ? max : w2.length;
            for (i = 0; i < n; ++i)
               System.out.print(w2[i].getWord() + " ");
            System.out.println();
         }
      }
   }
}
