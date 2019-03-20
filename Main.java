import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.text.Normalizer;
@SuppressWarnings("unchecked")

public class Main {

	public static ArrayList<Double> C; //contenus
	public static ArrayList<Integer> L; //lignes
	public static ArrayList<Integer> I; //indices
	public static Hashtable<Integer,Integer> idRank; //correspondance between the page id and it's id in the rank vector
	public static Hashtable<String, Integer> ht_titles;
	public static Hashtable<Integer, String> ht_titles_rev;
	public static Hashtable<String, Integer[]> dictionnary;
	public static Double[] rank;

	public static double FreqMin = 0.000001;
	public static int N = 20000; //Number of words in the dictionnary 
	public static int Compteur;


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

	public static String cleanStopWords(String s){
		String[] stop_words = {" a "," b "," c "," d "," e "," f "," g "," h "," i "," j "," k "," l ",
		" m "," n "," o "," p "," q "," r "," s "," t "," u "," v "," w "," x "," y "," z "," ai ",
		" aie "," aient "," aies "," ait "," alors "," as "," au "," aucun "," aura "," aurai ",
		" auraient "," aurais "," aurait "," auras "," aurez "," auriez "," aurions "," aurons "," ainsi ",
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
		" nouveaux "," on "," vont "," vos "," votre "," vous "," vu "," ca "," etaient "," etais "," etait ",
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

	public static Hashtable<String, Integer> createHashtableTitles(String file, String[] wanted){


		System.out.println("CREATING HT TITLES");
		int current_id_title = 0;
		Hashtable<String, Integer> ht = new Hashtable<String, Integer>();
		boolean text = false;
		boolean eligible = false;
		boolean balise = true;
		Compteur = 0;
		int compteur = 0;
		try{
			InputStream is = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			Pattern p = Pattern.compile("<title>(.+?)</title>");
			String title = null;
			String line = br.readLine();
			boolean first = false;

		while(line != null){ //while not end of file
			if(text){
				for(String s : line.split(" ")){
					s = normalize(s);
					if(s.contains("&lt")){
						balise = false;
					}else if(s.contains("&gt")){
						balise = true;
					}
					if(balise){
						for(String w : wanted){
							if(s.contains(normalize(w)) && first){
								text = false;
								eligible = true;
								first = false;

								if((compteur++)>100){
									System.out.println(100*(++Compteur)+"! ");
									compteur = 0;
								}
								ht.put(title,current_id_title);
								current_id_title++;		
								break;
							}
							if(!first){
								break;
							}
						}
					}
				}
			}else{
				Matcher m = p.matcher(line);

				if(m.find()){
						title = m.group(1); //we keep accents
						first = true;
					}
				}				
				if(line.contains("<text ")){
					text = true;
					eligible = false;
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

		System.out.println("HT TITLES CREATED");

		return ht;
	}

	public static Hashtable<String,Hashtable<Integer,Double>> createDictionnary(String file, Hashtable<String, Integer> ht_titles){
		
		System.out.println("CREATING DICTIONNARY");
		Hashtable<String,Hashtable<Integer,Double>> dict = new Hashtable<String,Hashtable<Integer,Double>>();
		Hashtable<Integer,Integer> ht_nb_words = new Hashtable<Integer,Integer>();
		try{
			InputStream is = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			C = new ArrayList<Double>();
			L = new ArrayList<Integer>();
			I = new ArrayList<Integer>();

			ArrayList<Integer> liens = new ArrayList<Integer>();

			String line = normalize(br.readLine());
			//if the page is in ht_titles
			boolean eligible = false;
			//if we are inside of a text balise
			boolean eligible_text = false;
			String[] words;
			//current page id
			int current_id = 0; 
			//current index in array C
			int current_indice = 0;
			//number of words in the current page
			int nb_words = 0;
			//number of {{ open and not closed
			int nb_open = 0;
			int current_nb_titles = 0;
			//while not end of file
			while(line != null){
				if(line.length()>1){
					if(line.charAt(0) != '|' && line.charAt(1) != '|' && !line.contains("[http") && !line.contains("Fichier:")){
						if(line.contains("<title>")){
							eligible = false;
						}
						if(eligible){

							if(line.contains("<text ")){
								eligible_text = true;
							}

							if(line.contains("</text>")){
								for (Map.Entry<String,Hashtable<Integer,Double>> entry : dict.entrySet()) {
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

								ht_nb_words.put(current_id,nb_words);

								eligible_text = false;
								nb_words = 0;
							}

							if(eligible_text){
								nb_open += (line.length() - line.replace("{{", "").length())/2;
								nb_open -= (line.length() - line.replace("}}", "").length())/2;
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
										//title found in the ht_titles for the first time
										if(id_title != -1 && !liens.contains(id_title)){ 
											liens.add(id_title);
											I.add(id_title);
											current_nb_titles++;
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

											//if it's a new word
											if(!dict.containsKey(w)){
												Hashtable<Integer,Double> h = new Hashtable<Integer,Double>();
												h.put(current_id,1.0);
												dict.put(w,h);
											}else{
												//if we've already seen this word in this page
												if(dict.get(w).containsKey(current_id)){ 
													dict.get(w).put(current_id,dict.get(w).get(current_id)+1.0);
												}else{
													dict.get(w).put(current_id,1.0);
												}
											}
										}
									}
								}
							}
						}else{
							Pattern p = Pattern.compile("<title>(.+?)</title>");
							Matcher m = p.matcher(line);
							if(m.find()){
								//we keep accents
								String title = m.group(1);
								//if the page is store in ht_titles
								if(ht_titles.containsKey(title)){
									liens.clear();
									current_nb_titles = 0;
									nb_open = 0;
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

		System.out.println("DICTIONNARY CREATED");	
		storePartialDictionnary(dict);

		return selectNBestFreq(dict,ht_nb_words);
	}

	public static Hashtable<String,Hashtable<Integer,Double>> selectNBestFreq(
		Hashtable<String,Hashtable<Integer,Double>> dict,
		Hashtable<Integer,Integer> ht_nb_words){
		Hashtable<String,Hashtable<Integer,Double>> result = new Hashtable<String,Hashtable<Integer,Double>>();
		ArrayList<Word> words = new ArrayList<Word>();
		int i = 0;

		for (String word : dict.keySet()) {
			words.add(i,new Word(word));
			for(Map.Entry<Integer,Double> e : dict.get(word).entrySet()){
				words.get(i).addOcc((int)(e.getValue()*ht_nb_words.get(e.getKey())));
			}
			i++;
		}

		Collections.sort(words, new SortByNbOccRev());
		System.out.println("word size "+words.size());
		if(words.size() < N){
			for(int j = 0; j < words.size(); j++){
				result.put(words.get(j).getText(),dict.get(words.get(j).getText()));
			}
		}else{
			for(int j = 0; j < N; j++){
				result.put(words.get(j).getText(),dict.get(words.get(j).getText()));
			}
		}


		return result;
	}

	public static String titleToURL(String title){
		title = title.replaceAll(" ","_");
		title = "https://fr.wikipedia.org/wiki/"+title;
		return title;
	}

	public static ArrayList<Integer> getRandomList(int k, int n){
		ArrayList<Integer> list = new ArrayList<Integer>();

		for(int i = 0; i < n ; i++){
			list.add(i);
		}

		return list;
	}

	public static Double[] rankVector(Double[] prev_rank, boolean first){
		// System.out.println("STARTING RANK CALCUL ! ");
		int n = prev_rank.length;
		Double rank [] = new Double[n];
		int max = n-1;
		int k = 1000;
		int id = 0;
		//Initialisation of the Rank vector
		for(int p = 0; p < n; p++){
			rank[p] = 0.0;
		}
		//Calcul a new step of Rank vector
		for(int i = 0; i < L.size()-1; i++){
			if(first){
				idRank.put(id,L.get(i));
				id++;
			}
			//if it's not a ligne full of zero
			if(L.get(i) != L.get(i+1)){
				for(int j = L.get(i); j < L.get(i+1); j++){
					rank[I.get(j)] += C.get(j)*prev_rank[i];
				}
			}else{
				ArrayList<Integer> rand = getRandomList(k,n);
				for(Integer a : rand){
					rank[a] += prev_rank[i]*1.0/k;
				}
			}
		}

		// System.out.println("RANK CALCULED ! ");

		return rank;
	}

	public static Double distance(Double[] v, Double[] w){
		Double sum = 0.0;
		for(int i = 0; i < v.length; i++){
			sum += Math.abs(v[i]-w[i]);
		}

		return sum;
	}

	//Sort the dictionnary and convert it to a Hashtable<String, Integer[]> sorted
	public static Hashtable<String, Integer[]> sortDictionnary(Hashtable<String,Hashtable<Integer,Double>> dictionnary, Double[] rank){

		Hashtable<String, Integer[]> dict = new Hashtable<String, Integer[]>();

		for(Map.Entry<String,Hashtable<Integer,Double>> e : dictionnary.entrySet()){
			dict.put(e.getKey(),sortPagesByRank(e.getValue()));
		}

		return dict;
	}

	public static Integer[] sortPagesByRank(Hashtable<Integer,Double> ht){
		Integer[] result = new Integer[ht.size()];
		ArrayList<Map.Entry<Integer, Double>> l = new ArrayList<>(ht.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<Integer,Double>>(){
			public int compare(Map.Entry<Integer,Double> o1, Map.Entry<Integer,Double> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}});

		for(int i = 0; i < l.size(); i++){
			if(l.get(i).getValue() < FreqMin){
				break;
			}
			result[i] = l.get(i).getKey();
		}

		return result;
	}

	public static ArrayList<Integer> researchSimple(String word, Hashtable<String, Integer[]> dict){
		ArrayList<Integer> result = new ArrayList<Integer>();
		if(dict.containsKey(word)){
			for (Integer i : dict.get(word)){
				result.add(i);
			}
		}
		return result;
	}

	public static ArrayList<Integer> intersection(ArrayList<Integer> list1, ArrayList<Integer> list2){
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (Integer t : list1) {
			if(list2.contains(t)) {
				result.add(t);
			}
		}
		return result;
	}

	public static ArrayList<Integer> intersection2(ArrayList<Integer> list, Integer[] tab, Double[] rank){

		ArrayList<Integer> result = new ArrayList<Integer>();
		int i_tab = 0;
		int i_list = 0;
		while(i_tab < tab.length && i_list < list.size()){
			System.out.println("Try to get list : "+list.get(i_list)+" tab : "+tab[i_tab]);
			System.out.println("inter list "+rank[idRank.get(list.get(i_list))]+" tab "+rank[idRank.get(tab[i_tab])]);
			if(list.get(i_list) == tab[i_tab]){
				System.out.println("inter add "+tab[i_tab]);
				result.add(tab[i_tab]);
				i_tab ++;
				i_list++;
			}else{
				if(rank[idRank.get(list.get(i_list))] == rank[idRank.get(tab[i_tab])]){
					System.out.println("inter debut i_list " +i_list+" i_tab "+i_tab);
					double target = rank[idRank.get(list.get(i_list))];
					System.out.println("inter equal "+target);
					ArrayList<Integer> i_l = new ArrayList<Integer>();
					ArrayList<Integer> i_t = new ArrayList<Integer>();
					while(rank[idRank.get(list.get(i_list))] == target){
						i_l.add(i_list);
						i_list++;
						if(i_list == list.size()){
							break;
						}
					}
					while(rank[idRank.get(tab[i_tab])] == target){
						i_t.add(i_tab);
						i_tab++;
						if(i_tab == tab.length){
							break;
						}
					}
					for (Integer t : i_t) {
						if(i_l.contains(t)) {
							System.out.println("inter add "+t);
							result.add(t);
						}
					}
					System.out.println("inter fin i_list " +i_list+" i_tab "+i_tab);
				}else if (rank[idRank.get(list.get(i_list))] > rank[idRank.get(tab[i_tab])]){
					i_list++;
				}else{ 
					i_tab++;
				}
			}
		}
		return result;		
	}

	public static ArrayList<Integer> researchMultiple(ArrayList<String> words, Hashtable<String, Integer[]> dict, Double[] rank){
		ArrayList<Integer> result = new ArrayList<Integer>();

		if(words.size() != 0){
			result = researchSimple(words.get(0),dict);
			if(words.size() == 1){
				return result;
			}
			for(int i = 1; i < words.size(); i++){
				if(result.size() != 0){
					result = intersection(result,researchSimple(words.get(i),dict));
				}else{
					System.out.println("The word \""+words.get(i-1)+"\" doesn't exists in the dictionnary");
				}
			}
		}

		return result;
	}

	public static Hashtable<Integer, String> reverseHtTitle(Hashtable<String, Integer> ht_title){
		Hashtable<Integer, String> result = new Hashtable<Integer, String>();
		for(Map.Entry<String,Integer> e : ht_title.entrySet()){
			result.put(e.getValue(),e.getKey());
		}
		return result;
	}

	public static void printURLS(ArrayList<Integer> list, Hashtable<Integer, String> ht_titles){
		if(list.size() == 0){
			System.out.println("No result found");
			return;
		}
		for(Integer i : list){
			System.out.println(titleToURL(ht_titles.get(i)));
		}
	}

	public static void storeHtTitle(){
		try {
			FileOutputStream f_ht_title = new FileOutputStream("HtTitle.data");
			ObjectOutputStream o_ht_title = new ObjectOutputStream(f_ht_title);
			o_ht_title.writeObject(ht_titles);
			o_ht_title.close();
			f_ht_title.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void storePartialDictionnary(Hashtable<String,Hashtable<Integer,Double>> dict){
		try {
			FileOutputStream f_p_dict = new FileOutputStream("PartialDictionnary.data");
			ObjectOutputStream o_p_dict = new ObjectOutputStream(f_p_dict);
			o_p_dict.writeObject(dict);
			o_p_dict.close();
			f_p_dict.close();
			System.out.println("PARTIAL DICTIONNARY STORED");
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void storeDatas(){
		try {

			FileOutputStream f_ht_title = new FileOutputStream("HtTitle.data");
			ObjectOutputStream o_ht_title = new ObjectOutputStream(f_ht_title);
			o_ht_title.writeObject(ht_titles);
			o_ht_title.close();
			f_ht_title.close();

			FileOutputStream f_ht_title_rev = new FileOutputStream("HtTitleRev.data");
			ObjectOutputStream o_ht_title_rev = new ObjectOutputStream(f_ht_title_rev);
			o_ht_title_rev.writeObject(ht_titles_rev);
			o_ht_title_rev.close();
			f_ht_title_rev.close();

			FileOutputStream f_dictionnary = new FileOutputStream("Dictionnary.data");
			ObjectOutputStream o_dictionnary = new ObjectOutputStream(f_dictionnary);
			o_dictionnary.writeObject(dictionnary);
			o_dictionnary.close();
			f_dictionnary.close();

			FileOutputStream f_idRank = new FileOutputStream("idRank.data");
			ObjectOutputStream o_idRank = new ObjectOutputStream(f_idRank);
			o_idRank.writeObject(idRank);
			o_idRank.close();
			f_idRank.close();

			FileOutputStream f_rank = new FileOutputStream("Rank.data");
			ObjectOutputStream o_rank = new ObjectOutputStream(f_rank);
			o_rank.writeObject(rank);
			o_rank.close();
			f_rank.close();

		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void readHtTitle(){
		try{
			FileInputStream f_ht_title = new FileInputStream("HtTitle.data");
			ObjectInputStream o_ht_title = new ObjectInputStream(f_ht_title);
			ht_titles = (Hashtable<String, Integer>)o_ht_title.readObject();
			o_ht_title.close();
			f_ht_title.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Hashtable<String,Hashtable<Integer,Double>> readPartialDictionnary(){
		Hashtable<String,Hashtable<Integer,Double>> dict = new Hashtable<String,Hashtable<Integer,Double>>();
		try{
			FileInputStream f_p_dict = new FileInputStream("PartialDictionnary.data");
			ObjectInputStream o_p_dict = new ObjectInputStream(f_p_dict);
			dict = (Hashtable<String,Hashtable<Integer,Double>>)o_p_dict.readObject();
			o_p_dict.close();
			f_p_dict.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dict;
	}

	public static void readDatas(){
		try {

			FileInputStream f_rank = new FileInputStream("Rank.data");
			ObjectInputStream o_rank = new ObjectInputStream(f_rank);
			rank = (Double[])o_rank.readObject();
			o_rank.close();
			f_rank.close();

			FileInputStream f_id_rank = new FileInputStream("idRank.data");
			ObjectInputStream o_id_rank = new ObjectInputStream(f_id_rank);
			idRank = (Hashtable<Integer,Integer>)o_id_rank.readObject();
			o_id_rank.close();
			f_id_rank.close();

			FileInputStream f_ht_title = new FileInputStream("HtTitle.data");
			ObjectInputStream o_ht_title = new ObjectInputStream(f_ht_title);
			ht_titles = (Hashtable<String, Integer>)o_ht_title.readObject();
			o_ht_title.close();
			f_ht_title.close();

			FileInputStream f_ht_title_rev = new FileInputStream("HtTitleRev.data");
			ObjectInputStream o_ht_title_rev = new ObjectInputStream(f_ht_title_rev);
			ht_titles_rev = (Hashtable<Integer, String>)o_ht_title_rev.readObject();
			o_ht_title_rev.close();
			f_ht_title_rev.close();

			FileInputStream f_dictionnary = new FileInputStream("Dictionnary.data");
			ObjectInputStream o_dictionnary = new ObjectInputStream(f_dictionnary);
			dictionnary = (Hashtable<String, Integer[]>)o_dictionnary.readObject();
			o_dictionnary.close();
			f_dictionnary.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void initDatas(String file, String[] wanted, boolean init_ht_title, boolean init_part_dict){

		if(init_ht_title){
			ht_titles = createHashtableTitles(file,wanted);
			storeHtTitle();
			System.out.println("HT TITLES STORED");
		}else{
			readHtTitle();
		}
		ht_titles_rev = reverseHtTitle(ht_titles);
		Hashtable<String,Hashtable<Integer,Double>> dict;
		if(init_part_dict){
			dict = createDictionnary(file,ht_titles);			
		}else{
			dict = readPartialDictionnary();
		}
		idRank = new Hashtable<Integer,Integer>();

		int n = ht_titles.size();
		rank = new Double[n];
		//Initialisation of the Rank vector
		for(int k = 0; k < n; k++){
			rank[k] = 1.0/n;
		}
		Double epsilone = 0.001;
		Double rank2 [] = rankVector(rank,true);
		int i = 0;
		while(distance(rank,rank2) >= epsilone){
			i++;
			rank = rank2;
			rank2 = rankVector(rank2,false);
		}
		rank = rank2;

		dictionnary = sortDictionnary(dict,rank);

		storeDatas();
	}

	public static void main(String[] args) {

		// String file = "test.txt";
		// String file = "frwiki-20190120-pages-articles.xml";
		String file = "frwiki-debut.xml";
		// String file = "frwiki-18000000ll.xml";

		String [] wanted = {"mathematiques","informatique","sciences","etudiant"};

		Scanner scan = new Scanner(System.in);
		String str = "";
		while(!str.equals("read") && !str.equals("init")){
			System.out.println("Do you want to initialize datas (init) or read data (read) ?");
			str = scan.nextLine();
			if(str.equals("init")){
				initDatas(file,wanted,true,true);
			}else if(str.equals("read")){
				readDatas();
			}else{
				System.out.println("Wrong answer, usage : init OR read");				
			}	
		}
		ArrayList<String> words = new ArrayList<String>();
		while(true){
			System.out.println("Enter your research : ");
			str = cleanStopWords(normalize(scan.nextLine()));
			for(String w : str.split(" ")){
				words.add(w);
			}
			long time = System.currentTimeMillis();
			ArrayList<Integer> inter = researchMultiple(words, dictionnary, rank);
			time = System.currentTimeMillis() - time;
			System.out.print("There is "+inter.size()+" results for \""+str+"\"");
			System.out.println(" -> Search time : "+ time +" millisecond");
			System.out.println(" ");
			printURLS(inter,ht_titles_rev);
			System.out.println("---------------------");
			words.clear();
		}
	}
} 