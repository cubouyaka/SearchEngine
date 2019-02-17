import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.text.Normalizer;


public class Main {

	public static ArrayList<Double> C; //contenus
	public static ArrayList<Integer> L; //lignes
	public static ArrayList<Integer> I; //indices

	public static int N = 300; //Number of words in the dictionnary 


	public static class Word {
		String text;
		int nb_occ;

		public Word(String t){
			text = t;
			nb_occ = 0;
		}

		public String getText(){
			return text;
		}

		public int getNbOcc(){
			return nb_occ;
		}

		public void addOcc(int n){
			nb_occ += n;
		}
	}

	public static class SortByNbOccRev implements Comparator<Word>{

		@Override
		public int compare(Word a, Word b){
			return b.getNbOcc() - a.getNbOcc();
		}
	}

	public static String normalize(String s){
		s = Normalizer.normalize(s,Normalizer.Form.NFD).replaceAll("[\u0300-\u036F]","").toLowerCase();
		s = s.replaceAll("<"," <");
		s = s.replaceAll(">","> ");
		s = s.replaceAll("	"," ");
		s = s.replaceAll("&quot"," ");
		s = s.replaceAll("\\|"," ");
		s = s.replaceAll("\\[\\["," [[");
		s = s.replaceAll("\\]\\]","]] ");
		s = s.replaceAll("\\{\\{"," {{ ");
		s = s.replaceAll("\\}\\}"," }} ");

		Pattern p = Pattern.compile("\\{\\{(.+?)\\}\\}");
		Matcher m = p.matcher(s);
		while(m.find()){
			s = s.replaceAll(Pattern.quote(m.group()),"");
		}

		p = Pattern.compile("&lt;math&gt;(.+?)&lt;/math&gt;");
		m = p.matcher(s);
		while(m.find()){
			s = s.replaceAll(Pattern.quote(m.group()),"");
		}

		p = Pattern.compile("&lt;ref&gt;(.+?)&lt;/ref&gt;");
		m = p.matcher(s);
		while(m.find()){
			s = s.replaceAll(Pattern.quote(m.group()),"");
		}

		// p = Pattern.compile("&lt(.+?)/&gt;");
		p = Pattern.compile("&lt(.+?)&gt;");
		m = p.matcher(s);
		while(m.find()){
			s = s.replaceAll(Pattern.quote(m.group()),"");
		}

		p = Pattern.compile("\\=\\=(.+?)\\=\\=");
		m = p.matcher(s);
		while(m.find()){
			s = s.replaceAll(Pattern.quote(m.group()),"");
		}

		String[] ponctuation = {"\\’","«","»","\\[","\\]","…","\\/",":","\\'","\\.",";","\\!","\\?",",","\\-","\\(","\\)","\\*","\\=","%"};
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
		" m "," n "," o "," p "," q "," r "," s "," t "," u "," v "," w "," x "," y "," z "," ai ",
		" aie "," aient "," aies "," ait "," alors "," as "," au "," aucun "," aura "," aurai ",
		" auraient "," aurais "," aurait "," auras "," aurez "," auriez "," aurions "," aurons ",
		" auront "," aussi "," autre "," aux "," avaient "," avais "," avait "," avant "," avec ",
		" avez "," aviez "," avions "," avoir "," avons "," ayant "," ayez "," ayons "," bon "," car ",
		" ce "," ceci "," cela "," ces "," cet "," cette "," ceux "," chaque "," ci "," comme ",
		" comment "," dans "," de "," dedans "," dehors "," depuis "," des "," deux "," devoir ",
		" devrait "," devrez "," devriez "," devrions "," devrons "," devront "," dois "," doit ",
		" donc "," dos "," droite "," du "," debut "," du "," elle "," elles "," en "," encore ",
		" es "," est "," et "," eu "," eue "," eues "," eurent "," eus "," eusse "," eussent "," eusses ",
		" eussiez "," eussions "," eut "," eux "," eumes "," eut "," eutes "," faire "," fais "," faisez ",
		" fait "," faites "," fois "," font "," force "," furent "," fus "," fusse "," fussent "," fusses ",
		" fussiez "," fussions "," fut "," fumes "," fut "," futes "," haut "," hors "," ici "," il "," ils ",
		" je "," juste "," la "," le "," les "," leur "," leurs "," lui "," ma "," style ", "plus ",
		" maintenant "," mais "," me "," mes "," moi "," moins "," mon "," mot "," meme "," ne "," ni ",
		" nom "," nomme "," nommee "," nommes "," nos "," notre ", " nous "," nouveau ",
		" ont "," ou "," par "," parce "," parole "," pas "," personne "," personnes "," peu "," peut ",
		" plupart "," pour "," pourquoi "," qu "," quand "," que "," quel "," quelle "," quelles "," quels ",
		" qui "," sa "," sans "," se "," sera "," serai "," seraient "," serais "," serait "," seras ",
		" serez "," seriez "," serions "," serons "," seront "," ses "," seulement "," si "," sien "," soi ",
		" soient "," sois "," soit "," sommes "," son "," sont "," sous "," soyez "," soyons "," suis ",
		" sujet "," sur "," t "," ta "," tandis "," te "," tellement "," tels "," tes "," toi "," ton ",
		" tous "," tout "," trop "," tres "," tu "," un "," une "," valeur "," voient "," vois "," voit ",
		" nouveaux "," on "," vont "," vos "," votre "," vous "," vu "," ça "," etaient "," etais "," etait ",
		" etant "," etat "," etiez "," etions "," ete "," etes "," etes "," etre "," align "," text "," center "};

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
		Hashtable<Integer,Integer> ht_nb_words = new Hashtable<Integer,Integer>();
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
				if(line.length()>0){
					if(line.charAt(0) != '|' && !line.contains("[http")){
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
						// if(current_nb_titles == 0){
						// 	C.add(1.0);
						// 	current_indice++;
						// }

								ht_nb_words.put(current_id,nb_words);

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
											current_nb_titles++;
											System.out.println("!!!!"+title+"!!!");
										}
										else{
											System.out.println("-------"+title+"------");
										}
										line = line.replace(m.group()," ");
									}

									line = line.replaceAll("<text xml:space=\"preserve\">"," ");
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

return selectNBestFreq(dictionnary,ht_nb_words);
}

public static Hashtable<String,Hashtable<Integer,Double>> selectNBestFreq(Hashtable<String,Hashtable<Integer,Double>> dictionnary,
	Hashtable<Integer,Integer> ht_nb_words){
	Hashtable<String,Hashtable<Integer,Double>> result = new Hashtable<String,Hashtable<Integer,Double>>();
	ArrayList<Word> words = new ArrayList<Word>();
	int i = 0;

	for (String word : dictionnary.keySet()) {
		words.add(i,new Word(word));
		for(Map.Entry<Integer,Double> e : dictionnary.get(word).entrySet()){
			words.get(i).addOcc((int)(e.getValue()*ht_nb_words.get(e.getKey())));
		}
		i++;
	}

	Collections.sort(words, new SortByNbOccRev());

	for(int j = 0; j < N; j++){
		result.put(words.get(j).getText(),dictionnary.get(words.get(j).getText()));
	}

	return result;
}

public static String titleToURL(String title){
		// title = title.toLowerCase();
	title = title.replaceAll(" ","_");
	title = "https://fr.wikipedia.org/wiki/"+title;
	return title;
}

public static void main(String[] args) {

		// String file = "test.txt";
		// String file = "short.xml";
	String file = "frwiki-debut.xml";
		// String [] wanted = {"etudiant"};
	String [] wanted = {"mathematiques","informatique","sciences","etudiant"};
		//System.out.println(nbPagesThatContains("test.txt",wanted));

	Hashtable<String, Integer> ht_titles = createHashtableTitles(file,wanted);

		// System.out.println(idTitle(ht_titles,"Civilization"));

	Hashtable<String,Hashtable<Integer,Double>> dictionnary = createDictionnary(file,ht_titles);

		// printDictionnary(dictionnary);
	// System.out.println(dictionnary.keySet());
		System.out.println(ht_titles);

	// System.out.println(C);
	// System.out.println(L);
	// System.out.println(I);

		// System.out.println(ht_titles.toString());

		// System.out.println(nbPagesThatContains("frwiki-debut.xml",wanted));

	System.out.println("ht Title size : "+ht_titles.size());
		// System.out.println("Dictionnary Size : "+dictionnary.size());
}
} 