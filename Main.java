import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.text.Normalizer;


public class Main {

	// public static void tokenisation(String t){

	// 	Pattern p = Pattern.compile("<page>(.+?)</page>");
	// 	Matcher m = p.matcher(t);
	// 	// System.out.println(m);
	// 	int nb_pages = 0;
	// 	int nb_pages_ok = 0;
	// 	String [] wanted = {"un","deux","trois"};

	// 	while(m.find()){
	// 		String [] arr = m.group(1).split(" ");
	// 		for(String w : wanted){
	// 			if(Arrays.asList(arr).contains(w)){
	// 				nb_pages_ok ++;
	// 				break;
	// 			}
	// 		}

	// 		for(String s : arr){
	// 			if(!s.equals("")){
	// 				System.out.println(s);
	// 			}
	// 		}
	// 		nb_pages++;
	// 	} 
	// 	System.out.println(nb_pages_ok);
	// }

	public static ArrayList<Double> C; //contenus
	public static ArrayList<Integer> L; //lignes
	public static ArrayList<Integer> I; //indices

	public static String normalize(String s){
		s = Normalizer.normalize(s,Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]","").toLowerCase();
		s = s.replaceAll("<"," <");
		s = s.replaceAll(">","> ");
		s = s.replaceAll("	"," ");
		s = s.replaceAll("&quot"," ");
		s = s.replaceAll("\\[\\["," [[");
		s = s.replaceAll("\\]\\]","]] ");
		s = s.replaceAll("\\{\\{"," {{ ");
		s = s.replaceAll("\\}\\}"," }} ");

		Pattern p = Pattern.compile("\\{\\{(.+?)\\}\\}");
		Matcher m = p.matcher(s);
		while(m.find()){
			s = s.replaceAll(Pattern.quote(m.group()),"");
		}

		p = Pattern.compile("&lt;ref&gt;(.+?)&lt;/ref&gt;");
		m = p.matcher(s);
		while(m.find()){
			s = s.replaceAll(Pattern.quote(m.group()),"");
		}

		p = Pattern.compile("&lt;(.+?)/&gt;");
		m = p.matcher(s);
		while(m.find()){
			s = s.replaceAll(Pattern.quote(m.group()),"");
		}

		p = Pattern.compile("&lt;math&gt;(.+?)&lt;/math&gt;");
		m = p.matcher(s);
		while(m.find()){
			s = s.replaceAll(Pattern.quote(m.group()),"");
		}

		p = Pattern.compile("\\=\\=(.+?)\\=\\=");
		m = p.matcher(s);
		while(m.find()){
			s = s.replaceAll(Pattern.quote(m.group()),"");
		}

		String[] ponctuation = {"\\’","\\'","\\.",";","\\!","\\?",",","\\-","\\(","\\)","\\*","\\=","%"};
		for(String sp : ponctuation){
			s = s.replaceAll(sp," ");
		}

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
		int current_id_title = 0;
		Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
		try{
			InputStream is = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line = br.readLine();

			while(line != null){ //while not end of file
				Pattern p = Pattern.compile("<title>(.+?)</title>");
				Matcher m = p.matcher(line);

				if(m.find()){
					// String title = normalize(m.group(1)); //we dont keep accents
					String title = m.group(1); //we keep accents
					line = br.readLine();

					if(ht.containsKey(title)){
						System.out.println("Warning : Titre en double: "+title+" current id: "+
							current_id_title+" previous id: "+ht.get(title));
					}
					ht.put(title,current_id_title);
					current_id_title++;
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
		String[] stop_words = {" a "," b "," c "," d "," e "," f "," g "," h "," i "," j "," k "," l ",
		" m "," n "," o "," p "," q "," r "," s "," t "," u "," v "," w "," x "," y "," z ","trois","deux"};
		for(String sw : stop_words){
			s = s.replaceAll(sw," ");
		}
		return s;
	}

	public static void printDictionnary(Hashtable<String,Hashtable<Integer,Double>> dictionnary){
		System.out.println(dictionnary.toString());
	}

	public static int idTitle(Hashtable<String, Integer> ht_titles, String title){
		if(!ht_titles.containsKey(title)){
			return -1; //not found
		}

		return ht_titles.get(title);
	}

	public static Hashtable<String,Hashtable<Integer,Double>> createDictionnary(String file, Hashtable<String, Integer> ht_titles){
		Hashtable<String,Hashtable<Integer,Double>> dictionnary = new Hashtable<String,Hashtable<Integer,Double>>();
		try{
			InputStream is = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			C = new ArrayList<Double>();
			L = new ArrayList<Integer>();
			I = new ArrayList<Integer>();

			String line = normalize(br.readLine());
			boolean eligible = false; //if the page is in ht_titles
			boolean eligible_text = false; //if we are inside of a text balise
			String[] words;
			int current_id = 0; //current page id 
			int current_indice = 0; //current index in array C
			int nb_words = 0; //number of words in the current page
			int nb_open = 0; //number of {{ open and not closed
			int current_nb_titles = 0;

			while(line != null){ //while not end of file
				if(line.contains("<title>")){
					eligible = false;
				}
				if(eligible){

					if(line.contains("<text ")){
						eligible_text = true;
					}

					if(line.contains("</text>")){
						for (Map.Entry<String,Hashtable<Integer,Double>> entry : dictionnary.entrySet()) {
							for(Map.Entry<Integer,Double> e : entry.getValue().entrySet()){
								if(e.getKey() == current_id){
									e.setValue(e.getValue()/nb_words);
								}
							}
						}

						for(int i = 0; i < current_nb_titles; i++){
							C.add((Double)(1.0/current_nb_titles));
							current_indice++;
						}

						eligible_text = false;
						nb_words = 0;
					}

					if(eligible_text){
						nb_open += (line.length() - line.replace("{{", "").length())/2;
						if(nb_open == 0){
							Pattern p = Pattern.compile("\\[\\[(.+?)\\]\\]");
							Matcher m = p.matcher(line);

							while(m.find()){
								String title = m.group(1);
								p = Pattern.compile("(.+?)\\|");
								Matcher m2 = p.matcher(title);
								if(m2.find()){
									title = m2.group(1);
								}
								int id_title = idTitle(ht_titles,title);
								if(id_title != -1){ //title found in the ht_titles
									I.add(id_title);
									current_id++;
									current_nb_titles++;
								}
							}

							line = normalize(line);
							line = cleanStopWords(line);
							words = line.split(" ");
							for(String w : words){
								if(!w.equals("")){
									nb_words++;
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
					}
					nb_open -= (line.length() - line.replace("}}", "").length())/2;
				}
			}else{
				Pattern p = Pattern.compile("<title>(.+?)</title>");
				Matcher m = p.matcher(line);
				if(m.find()){
						String title = m.group(1); //we keep accents
						if(ht_titles.containsKey(title)){ //if the page is store in ht_titles
							current_nb_titles = 0;
							L.add(current_indice);
							current_id = ht_titles.get(title);
							eligible = true;
						}
					}
				}
				line = br.readLine();
			}
			L.add(current_indice);
		}catch(FileNotFoundException e){
			System.err.println("Caught FileNotFoundException: " + e.getMessage());
		} catch(IOException e) {                                                                                                                                                                                                                                                          
			System.err.println("Caught IOException: " + e.getMessage());                                                                                                                                                                                                                    
		}

		return dictionnary;
	}

	public static void main(String[] args) {

		// String file = "test.txt";
		String file = "short.xml";
		// String file = "frwiki-debut.xml";
		String [] wanted = {"etudiant"};
		// String [] wanted = {"mathematiques","informatique","sciences"};
		//System.out.println(nbPagesThatContains("test.txt",wanted));

		Hashtable<String, Integer> ht_titles = createHashtableTitles(file,wanted);

		Hashtable<String,Hashtable<Integer,Double>> dictionnary = createDictionnary(file,ht_titles);

		// printDictionnary(dictionnary);

		System.out.println(C);
		System.out.println(L);
		System.out.println(I);

		// System.out.println(ht_titles.toString());

		// System.out.println(nbPagesThatContains("frwiki-debut.xml",wanted));

		// System.out.println(ht_titles.size());
		// System.out.println(dictionnary.size());

		// if(dictionnary.containsKey("namespace")){
		// 	System.out.println("namespace");
		// }
		// if(dictionnary.containsKey("<namespace")){
		// 	System.out.println("<namespace");
		// }
		// if(dictionnary.containsKey("comparative")){
		// 	System.out.println("comparative");
		// }
		// if(dictionnary.containsKey("152515873")){
		// 	System.out.println("152515873");
		// }



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