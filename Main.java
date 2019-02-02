import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.text.Normalizer;

public class Main {

	public static void tokenisation(String t){

		Pattern p = Pattern.compile("<page>(.+?)</page>");
		Matcher m = p.matcher(t);
		// System.out.println(m);
		int nb_pages = 0;
		int nb_pages_ok = 0;
		String [] wanted = {"un","deux","trois"};

		while(m.find()){
			String [] arr = m.group(1).split(" ");
			for(String w : wanted){
				if(Arrays.asList(arr).contains(w)){
					nb_pages_ok ++;
					break;
				}
			}

			for(String s : arr){
				if(!s.equals("")){
					System.out.println(s);
				}
			}
			nb_pages++;
		} 
		System.out.println(nb_pages_ok);
	}

	public static String normalize(String s){
		s = Normalizer.normalize(s,Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]","").toLowerCase();
		s = s.replaceAll("<"," <");
		s = s.replaceAll(">","> ");
		return s;
	}

	public static int nbPagesThatContains(String file, String[] wanted){
		int nb = 0;

		try{
			InputStream is = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = br.readLine();
			boolean text = false; //if we are inside a text balise
			boolean balise = true; //if we are reading a balise

			while(line != null){ //while not end of file
				for(String w : wanted){
					if(text){
						for(String s : line.split(" ")){
							s = normalize(s);
							if(s.contains("&lt")){
								balise = false;
							}else if(s.contains("&gt")){
								balise = true;
							}
							if(balise){
								if(s.contains(w.toLowerCase())){
									nb ++;
									text = false;
									break;
								}
							}
						}				
					}
				}
				if(line.contains("<text ")){
					text = true;
				}else if (line.contains("</text>")){
					text = false;
				}

				line = br.readLine();
			}
		}catch(FileNotFoundException e){
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
		} catch(IOException e) {                                                                                                                                                                                                                                                          
			System.err.println("Caught IOException: " + e.getMessage());                                                                                                                                                                                                                    
		}

		return nb;
	}

	public static Hashtable<String, Integer> createHashtableTitles(String file, String[] wanted){
		int i = 0;
		Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
		try{
			InputStream is = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = br.readLine();

			while(line != null){ //while not end of file
				Pattern p = Pattern.compile("<title>(.+?)</title>");
				Matcher m = p.matcher(line);

				//TODO les mots ne se trouvent qu'apres la balise <text ...> !

				if(m.find()){
					String title = normalize(m.group(1));
					line = br.readLine();

					if(ht.containsKey(title)){
						System.out.println("titre en double: "+title+" current id: "+i+" previous id: "+ht.get(title));
					}
					ht.put(title,i);
					i++;
				}
				line = br.readLine();
			}
		}catch(FileNotFoundException e){
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
		} catch(IOException e) {                                                                                                                                                                                                                                                          
			System.err.println("Caught IOException: " + e.getMessage());                                                                                                                                                                                                                    
		}

		return ht;
	}

	public static String cleanStopWords(String s){
		String[] stop_words = {"un","deux"};
		for(String sw : stop_words){
			s = s.replaceAll(sw,"");
		}
		return s;
	}

	public static void printDictionnary(Hashtable<String,Hashtable<Integer,Double>> dictionnary){
		System.out.println(dictionnary.toString());
	}

	public static Hashtable<String,Hashtable<Integer,Double>> createDictionnary(String file, Hashtable<String, Integer> ht_titles){
		Hashtable<String,Hashtable<Integer,Double>> dictionnary = new Hashtable<String,Hashtable<Integer,Double>>();
		try{
			InputStream is = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = normalize(br.readLine());
			boolean eligible = false; //if the page is in ht_titles
			boolean eligible_text = false; //if we are inside of a text balise
			String[] words;
			int current_id = 0; //current page id 
			int nb_words = 0; //number of words in the current page
			int nb_open = 0; //number of {{ open and not closed

			while(line != null){ //while not end of file
				if(line.contains("<title>")){
					eligible = false;
				}
				if(eligible){

					if(line.contains("<text ")){
						eligible_text = true;
					}
					if(eligible_text){
						if(line.contains("{{")){
							nb_open++;
						}
						if(line.contains("}}")){
							nb_open--;
						}
						if(nb_open == 0){
							line = normalize(line);
							line = cleanStopWords(line);
							words = line.split(" ");
							for(String w : words){
								nb_words++;

							// TODO frequencies of each words in the current page

								if(!dictionnary.containsKey(w)){ //if it's a new word
								Hashtable<Integer,Double> h = new Hashtable<Integer,Double>();
								h.put(current_id,1.0);
								dictionnary.put(w,h);
								}else{ //if the word already exist in the dictionnary
									if(dictionnary.get(w).containsKey(current_id)){ //if we've already seen this word in this page
									dictionnary.get(w).put(current_id,dictionnary.get(w).get(current_id)+1.0);
								}else{
									dictionnary.get(w).put(current_id,1.0);
								}
							}
						}
					}
					if(line.contains("</text>")){
						eligible_text = false;
					}
				}
			}else{
				Pattern p = Pattern.compile("<title>(.+?)</title>");
				Matcher m = p.matcher(line);
				if(m.find()){
					String title = normalize(m.group(1));
						if(ht_titles.containsKey(title)){ //if the page is store in ht_titles
							current_id = ht_titles.get(title);
							eligible = true;
						}
					}
				}
				line = br.readLine();
			}
		}catch(FileNotFoundException e){
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
		} catch(IOException e) {                                                                                                                                                                                                                                                          
			System.err.println("Caught IOException: " + e.getMessage());                                                                                                                                                                                                                    
		}

		return dictionnary;
	}

	public static void main(String[] args) {

		// String file = "test.txt";
		String file = "debut.xml";
		// String file = "frwiki-debut.xml";
		String [] wanted = {"etudiant"};
		// String [] wanted = {"mathematiques","informatique","sciences"};
		//System.out.println(nbPagesThatContains("test.txt",wanted));

		Hashtable<String, Integer> ht_titles = createHashtableTitles(file,wanted);

		Hashtable<String,Hashtable<Integer,Double>> dictionnary = createDictionnary(file,ht_titles);

		// System.out.println(nbPagesThatContains("frwiki-debut.xml",wanted));

		// System.out.println(ht_titles.toString());

		if(dictionnary.containsKey("namespace")){
			System.out.println("namespace");
		}
		if(dictionnary.containsKey("<namespace")){
			System.out.println("<namespace");
		}
		if(dictionnary.containsKey("comparative")){
			System.out.println("comparative");
		}
		if(dictionnary.containsKey("152515873")){
			System.out.println("152515873");
		}
		
		// printDictionnary(dictionnary);


		// if(ht.get(normalize("Titre one")) != null){
		// 	System.out.println(ht.get(normalize("Titre one")));
		// }


		// ArrayList<Word> array = new ArrayList<Word>();
		// Word w1 = new Word("aribo");
		// Word w2 = new Word("betran");
		// Word w3 = new Word("ckjnon");
		// Word w4 = new Word("dneRTds");
		// Word w5 = new Word("Azzz");
		// Word w6 = new Word("ViejroRa");
		// Word w7 = new Word("ViejroRa");

		// array.add(w2);
		// array.add(w4);
		// array.add(w3);
		// array.add(w7);
		// array.add(w5);
		// array.add(w1);
		// array.add(w6);
		// Collections.sort(array, new SortAlphabetically());

		// for(Word w: array){
		// 	System.out.println(w.getText());
		// }
	}
} 







					// while(!line.contains("<text xml")){
					// 	line = br.readLine();
					// }
					// line = br.readLine();
					// int nb_open = 1;
					// while(nb_open != 0){
					// 	line = br.readLine();
					// 	if(line.contains("{{")){
					// 		nb_open++;
					// 	}
					// 	if(line.contains("}}")){
					// 		nb_open--;
					// 	}
					// }
