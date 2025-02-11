package LCE;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.config.Configurator;

public class Extractor {
    LCS lcs;
    private String gumtree_vector_path;
    private String target_vector_path;
    private String commit_file_path;

    private List<String> source_files;
    private int[] max_N_index_list;
    private List<List<String>> meta_pool_list = new ArrayList<>();
    private List<List<String>> cleaned_meta_pool_list = new ArrayList<>();
    private int nummax;
    private int threshold;

    static Logger extractionLogger = LogManager.getLogger(Extractor.class.getName());

    public Extractor(Properties argv) {
        super();
        Configurator.setLevel(Extractor.class, Level.TRACE);
        lcs = new LCS();
        gumtree_vector_path = argv.getProperty("pool_file.dir"); // gumtree vector path
        commit_file_path = argv.getProperty("meta_pool_file.dir"); // commit file path
        //System.out.println(commit_file_path);
        target_vector_path = argv.getProperty("target_vector.dir"); // target dir
        nummax = argv.getProperty("candidate_number").equals("") ? 10
                : Integer.parseInt(argv.getProperty("candidate_number")); // nummax
        threshold = argv.getProperty("threshold").equals("") ? 1000
                : Integer.parseInt(argv.getProperty("threshold")); // threshold
    }

    public void config(String pool_dir, String vector_dir, String meta_pool_dir, String result_dir) {
        this.gumtree_vector_path = pool_dir;
        this.target_vector_path = vector_dir;
        this.commit_file_path = meta_pool_dir;
    }

    public void run() {
        int[][] pool_array;
        int[][] vector_array;
        float[] sim_score_array;
        Vector<Integer> index_list_to_remove = new Vector<Integer>();
        try {
            pool_array = list_to_int_array2d(CSV_to_ArrayList(gumtree_vector_path));
            extractionLogger
                    .trace(App.ANSI_BLUE + "[status] original pool array size = " + pool_array.length + App.ANSI_RESET);

            meta_pool_list = CSV_to_ArrayList(commit_file_path);
            extractionLogger
                    .trace(App.ANSI_BLUE + "[status] meta pool list size = " + meta_pool_list.size() + App.ANSI_RESET);

            // remove empty lines
            int[][] cleaned_pool_array = remove_empty_lines(pool_array, index_list_to_remove);
            // remove lines with extra long vectors over threshold
            //int[][] regressed_pool_array = remove_extra_long_lines(cleaned_pool_array, index_list_to_remove, nummax);
            //[jh] version
            int[][] regressed_pool_array = remove_extra_long_lines(cleaned_pool_array, index_list_to_remove, 1000);
            
            cleaned_meta_pool_list = sync_removal(meta_pool_list, index_list_to_remove);
            extractionLogger.trace(
                    App.ANSI_BLUE + "[status] cleaned pool array size = " + regressed_pool_array.length
                            + App.ANSI_RESET);
            extractionLogger
                    .trace(App.ANSI_BLUE + "[status] index_list_to_remove size : " + index_list_to_remove.size()
                            + App.ANSI_RESET);
            extractionLogger
                    .trace(App.ANSI_BLUE + "[status] cleaned meta pool list size = " + cleaned_meta_pool_list.size()
                            + App.ANSI_RESET);

            // [jh] 확인용
            // for(int i=0;i<cleaned_meta_pool_list.size();i++){
            //     extractionLogger
            //         .trace(App.ANSI_BLUE + "[status] cleaned meta pool list = " + cleaned_meta_pool_list.get(i).get(5)
            //                 + App.ANSI_RESET);
            // }
            //
            vector_array = list_to_int_array2d(CSV_to_ArrayList(target_vector_path));

            sim_score_array = new float[regressed_pool_array.length];

            // score similarity on each line of pool and vector
            for (int i = 0; i < regressed_pool_array.length; i++) {
                sim_score_array[i] = lcs.ScoreSimilarity(regressed_pool_array[i], vector_array[0]);
            }
            HashMap<Float, Integer> sim_score_map = count_number_of_sim_scores(sim_score_array); // new
            // max_N_index_list = indexesOfTopElements(sim_score_array, nummax);
            max_N_index_list = index_of_candidate_patches(sim_score_array, sim_score_map, nummax);
            //[jh] version
            //max_N_index_list = index_of_candidate_patches(sim_score_array, sim_score_map, threshold);
            extractionLogger.trace(
                    App.ANSI_BLUE + "[status] max_N_index_list size = " + max_N_index_list.length + App.ANSI_RESET);
        } catch (FileNotFoundException e) {
            extractionLogger.error(App.ANSI_RED + "[error] file not found exception" + App.ANSI_RESET); // ERROR
            e.printStackTrace();
        } catch (Exception e) {
            extractionLogger.error(App.ANSI_RED + "[error] exception" + App.ANSI_RESET); // ERROR
            e.printStackTrace();
        }
    }

    public List<String> extract() {
        source_files = new ArrayList<>();
        for (int i = 0; i < max_N_index_list.length; i++) {
            source_files.add(combine(cleaned_meta_pool_list.get(max_N_index_list[i])));
            // print the result
            extractionLogger.trace(App.ANSI_BLUE + "[result] " + source_files.get(i) + App.ANSI_RESET);
        }
        return source_files;
    }

    private List<List<String>> CSV_to_ArrayList(String filename) throws FileNotFoundException {
        List<List<String>> records = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(filename));) {
            while (scanner.hasNextLine()) {
                records.add(getRecordFromLine(scanner.nextLine()));
            }
        } catch (FileNotFoundException e) {
            extractionLogger
                    .error(App.ANSI_RED + "[status] file not found : " + App.ANSI_YELLOW + filename + App.ANSI_RESET); // DEBUG
            e.printStackTrace();
        }
        return records;
    }

    private List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(",");
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }

    private int[] array_to_int(String[] array) {
        int[] converted = new int[array.length];
        // convert array to int array
        for (int i = 0; i < array.length; i++) {
            converted[i] = Integer.parseInt(array[i]);
        }
        // return int array
        return converted;
    }

    private int[][] list_to_int_array2d(List<List<String>> list) {
        int[][] array = new int[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            array[i] = array_to_int(list.get(i).toArray(new String[list.get(i).size()]));
        }
        return array;
    }

    private String combine(List<String> list) {
        String combined = "";
        for (int i = 0; i < list.size(); i++) {
            combined += list.get(i) + ",";
        }
        // remove last comma of combined
        combined = combined.substring(0, combined.length() - 1);
        return combined;
    }

    // locate and remove lines with vector size over given threshold
    private int[][] remove_extra_long_lines(int[][] pool, Vector<Integer> index_list, int threshold) {
        List<int[]> new_pool = new ArrayList<int[]>();
        for (int i = 0; i < pool.length; i++) {
            if (pool[i].length <= threshold) {
                new_pool.add(pool[i]);
            } else {
                index_list.add(i);
            }
        }
        int[][] new_pool_array = new int[new_pool.size()][];
        for (int i = 0; i < new_pool.size(); i++) {
            new_pool_array[i] = new_pool.get(i);
        }
        return new_pool_array;
    }

    // locate and remove empty lines in pool
    private int[][] remove_empty_lines(int[][] pool, Vector<Integer> index_list) {
        List<int[]> new_pool = new ArrayList<>();
        for (int i = 0; i < pool.length; i++) {
            if (pool[i].length != 0) {
                new_pool.add(pool[i]);
            } else {
                index_list.add(i);
            }
        }
        int[][] new_pool_array = new int[new_pool.size()][];
        for (int i = 0; i < new_pool.size(); i++) {
            new_pool_array[i] = new_pool.get(i);
        }
        return new_pool_array;
    }

    private List<List<String>> sync_removal(List<List<String>> meta_pool, Vector<Integer> index_list) {
        List<List<String>> synced_meta_pool = new ArrayList<>();
        for (int i = 0; i < meta_pool.size(); i++) {
            if (!index_list.contains(i)) {
                synced_meta_pool.add(meta_pool.get(i));
            } else {
                extractionLogger.trace("[debug] index " + i + " is removed"); // DEBUG
            }
        }
        return synced_meta_pool;
    }

    // LEGACY
    // find index of top N max values in array and return index list
    private int[] indexesOfTopElements(float[] orig, int nummax) {
        float[] copy = Arrays.copyOf(orig, orig.length);
        Arrays.sort(copy);
        float[] honey = Arrays.copyOfRange(copy, copy.length - nummax, copy.length);
        int[] result = new int[nummax];
        int resultPos = 0;
        for (int i = 0; i < orig.length; i++) {
            float onTrial = orig[i];
            int index = Arrays.binarySearch(honey, onTrial);
            if (index < 0)
                continue;
            if (resultPos < nummax)
                result[resultPos++] = i;
        }
        return result;
    }

    // count number of each similarity score
    private HashMap<Float, Integer> count_number_of_sim_scores(float[] orig) {
        HashMap<Float, Integer> result = new HashMap<>();
        for (int i = 0; i < orig.length; i++) {
            float onTrial = orig[i];
            if (result.containsKey(onTrial)) {
                result.put(onTrial, result.get(onTrial) + 1);
            } else {
                result.put(onTrial, 1);
            }
        }
        return result;
    }

    // return index of specific similarity score
    private int[] indexes_of_specific_sim_score(float[] orig, HashMap<Float, Integer> map, float targetScore) {
        int[] result = new int[map.get(targetScore)];
        int resultPos = 0;
        for (int i = 0; i < orig.length; i++) {
            float onTrial = orig[i];
            if (onTrial == targetScore) {
                result[resultPos++] = i;
            }
        }
        return result;
    }
    
    private int[] index_of_candidate_patches(float[] orig, HashMap<Float, Integer> map, int nummax) {
        // if nummax = 10, then return index of top 10 max similarity scores
        int[] result = new int[nummax];
        int resultPos = 0;
        float targetScore = 0;
        Set keySet = map.keySet();
        Iterator keyIterator = keySet.iterator();
        float[] scores = new float[map.keySet().size()];
        int scorePos = 0;
        while(keyIterator.hasNext()){
            scores[scorePos] = (float) keyIterator.next();
            scorePos++;
        }
        scorePos = 0;
        // sort from highest to lowsest
        Arrays.sort(scores);
        while (resultPos < nummax) {
            int leftCandNum = nummax - resultPos;
            // if there is no more similarity score, then break
            if (map.isEmpty()) {
                break;
            }
            // if a similarity score is highest, and has more occurence than nummax, return
            // randomly picked indexes of that similarity score
            if (targetScore < scores[scorePos]) {
                targetScore = scores[scorePos];
            }
            // if left candidate number is higher than number of occurence of target score,
            // then return all indexes of target score
            if (leftCandNum >= map.get(targetScore)) {
                int[] indexes = indexes_of_specific_sim_score(orig, map, targetScore);
                for (int i = 0; i < indexes.length; i++) {
                    result[resultPos++] = indexes[i];
                }
                map.remove(targetScore);
                scorePos++;
            } else {
                // if left candidate number is lower than number of occurence of target score,
                // then return randomly picked indexes of target score
                int[] indexes = indexes_of_specific_sim_score(orig, map, targetScore);
                
                //[JH] version2 no_random
                //Seperate index by project
                
                String temp_project_name = new String();
                List<List<String>> seperatedIndex = new ArrayList<>();
                List<String> targetList;
                for(int i = 0; i < indexes.length; i++) {
                    targetList = cleaned_meta_pool_list.get(indexes[i]);
                    temp_project_name = targetList.get(5);
                    List<String> seperatedList;
                    String projectName;
                    int checkNewProject = 1;
                    for(int j = 0; j < seperatedIndex.size(); j++) {
                        extractionLogger.trace(App.ANSI_BLUE + "[status] i, j = " + i +" " + j
                            + App.ANSI_RESET);
                        seperatedList = seperatedIndex.get(j);
                        projectName = seperatedList.get(0);
                        if(projectName.equals(temp_project_name)) {
                            seperatedList.add(Integer.toString(indexes[i]));
                            checkNewProject = 0;
                            extractionLogger.trace(
                        App.ANSI_BLUE + "[status] seperatedList add = " + seperatedList.toString()
                            + App.ANSI_RESET);
                            break;
                        }
                    }
                    if(checkNewProject == 1) {
                        List<String> newProjectList = new ArrayList<>();
                        newProjectList.add(temp_project_name);
                        seperatedIndex.add(newProjectList);
                        extractionLogger.trace(
                        App.ANSI_BLUE + "[status] newProject add = " + newProjectList.toString()
                            + App.ANSI_RESET);
                    }
                }
            
                //Extract index form projects one by one
                extractionLogger.trace(
                        App.ANSI_BLUE + "[status] Passed line " + 339
                            + App.ANSI_RESET);
                int count = 0;
                for(int i =0; count < leftCandNum ; i++) {
                    int order = i % seperatedIndex.size();
                    targetList = seperatedIndex.get(order);
                    int projectCount = targetList.size() - 1;
                    extractionLogger.trace(
                        App.ANSI_BLUE + "[status] targetList = " + targetList.toString()
                            + App.ANSI_RESET);      
                    if(projectCount >= 1) {
                        // int randomIndex = (int) (Math.random() * projectCount + 1);
                        // result[resultPos++] = Integer.parseInt(targetList.get(randomIndex));
                        result[resultPos++] = Integer.parseInt(targetList.get(1));
                        targetList.remove(1);
                        count++;
                    }
                }
                for(int z =0 ; z< result.length;z++) {
            extractionLogger.trace(
                    App.ANSI_BLUE + "[status] candidate result [" + z+  "] =  " + result[z]
                            + App.ANSI_RESET);
        }
                map.remove(targetScore);
                scorePos++;

                //capstone version

                // int[] randomIndexes = new int[leftCandNum];
                // for (int i = 0; i < leftCandNum; i++) {
                //     int randomIndex = (int) (Math.random() * indexes.length);
                //     randomIndexes[i] = indexes[randomIndex];
                // }
                // for (int i = 0; i < randomIndexes.length; i++) {
                //     result[resultPos++] = randomIndexes[i];
                // }
                // map.remove(targetScore);
                // scorePos++;
                
                //[JH] version1 random_no_duplicate
                // int[] randomIndexes = new int[leftCandNum];
                // for (int i = 0; i < leftCandNum; i++) {
                //     int randomIndex = (int) (Math.random() * indexes.length);
                //     int checkDuplicates = 0;
                //     for(int j = 0; j < randomindexes.length; j++) {
                //         if(randomIndexes[j] == indexes[randomIndex]) {
                //             checkDuplicates = 1;
                //             i--;
                //             break;
                //         }
                //     }
                //     if(checkDuplicates == 0){
                //         randomIndexes[i] = indexes[randomIndex];
                //     }
                // }
                // for (int i = 0; i < randomIndexes.length; i++) {
                //     result[resultPos++] = randomIndexes[i];
                // }
                // map.remove(targetScore);
                // scorePos++;
            }
        }
        return result;
    }
}
