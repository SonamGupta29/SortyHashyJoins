import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class hashJoin {
	
	// Total number of records a block can hold
	public static int M = 100;
	HashMap<String, HashMap <Integer, List<String >>> subLists = new HashMap<String, HashMap <Integer, List<String>>>();
	HashMap<String, List <String> > subFileList = new HashMap<String, List<String>>();
	BufferedReader bfr = null;
	String relation1 = null, relation2 = null;
	
	public void join(String R, String S, int memoryBlocks) throws IOException {
		
		//  create sublists for R and S each of size M 
		//	Get the handle to the relation R		
		//	get just name of relations, if it contains the
		
		relation1 = R;
		relation2 = S;
		
		if(R.contains("/")) {
			relation1 = R.substring(R.lastIndexOf("/"), R.length());
		}
		if(S.contains("/")) {
			relation2 = S.substring(S.lastIndexOf("/"), S.length());
		}		
		bfr = new BufferedReader(new FileReader(new File(R)));
		open(relation1, bfr, memoryBlocks, 1);
		bfr = new BufferedReader(new FileReader(new File(S)));
		open(relation2, bfr, memoryBlocks, 2);
		
		getnext();
		
		close(bfr);
	}

	private void open(String relation, BufferedReader bfr, int memoryBlocks, int relNumber) throws IOException {
		
		String line = null;
		int hashCode = 0;
		
		//	Check out each and every bucket, if the bucket is full write it to the file		
		while((line = bfr.readLine()) != null) {
			
			//	Here we have considered that the relations R and S will have only two attributes
			if(relNumber == 1) { 
				hashCode = (line.split(" ")[1].trim().hashCode()) % (memoryBlocks - 1);
			} else {
				hashCode = (line.split(" ")[0].trim().hashCode()) % (memoryBlocks - 1);
			}
			// Check if the hashmap contains the relation as the key
			if(!subLists.containsKey(relation)) {
				HashMap <Integer, List<String >> subRelList = new HashMap <Integer, List<String> >();
				subLists.put(relation, subRelList);
			}
			// Now checking or adding key of relation check if the relation contains hashcode
			if(!subLists.get(relation).containsKey(hashCode)) {
				List<String> tempList = new ArrayList<String>();
				 subLists.get(relation).put(hashCode, tempList);
				 subLists.get(relation).get(hashCode).add(line);
			} else {
				subLists.get(relation).get(hashCode).add(line);
			}
			// If the bucket is full write it to the file
			if(subLists.get(relation).get(hashCode).size() == M) {				
				List <String> toWriteToFile = new ArrayList<String>(); 
				toWriteToFile = subLists.get(relation).get(hashCode);				
				// Create a file to write
				if(!subFileList.containsKey(relation)) {
					List <String> tempList = new ArrayList<String>();
					subFileList.put(relation, tempList);
				}				
				// Create a file if not there and get the handle
				if(!subFileList.get(relation).contains(String.valueOf(hashCode) + "_" + relation)) {
					subFileList.get(relation).add(String.valueOf(hashCode) + "_" + relation);
				}				
				BufferedWriter bfw = new BufferedWriter(new FileWriter(new File(String.valueOf(hashCode) + "_" + relation), true));				
				for(int i = 0; i < M; i++) {
					bfw.write(toWriteToFile.get(i) + "\n");
				}
				bfw.close();
				subLists.get(relation).get(hashCode).clear();
			}
		}
		
		//	Write all the remaining buckets which are not equal to M coz those are not written in file
		
		Iterator<Entry<Integer, List<String>>> entries = subLists.get(relation).entrySet().iterator();
		while (entries.hasNext()) {
		  Entry thisEntry = (Entry) entries.next();		  
		  if(subLists.get(relation).get(thisEntry.getKey()).size() == 0)
			  continue;
		  @SuppressWarnings("unchecked")
		  List <String> toWriteToFile = (List<String>) thisEntry.getValue();		  
		  // Check if the subfile file list contains the relation
		  if(!subFileList.containsKey(relation)) {
			  List <String> tempList = new ArrayList<String>();
			  subFileList.put(relation, tempList);
		  }
		  //Create a file if not there and get the handle
		  if(!subFileList.get(relation).contains(String.valueOf(thisEntry.getKey()) + "_" + relation)) {
			  subFileList.get(relation).add(String.valueOf(thisEntry.getKey()) + "_" + relation);
		  }
		  
		  BufferedWriter bfw = new BufferedWriter(new FileWriter(new File(String.valueOf(thisEntry.getKey()) + "_" + relation), true));				
		  for(int i = 0; i < subLists.get(relation).get(thisEntry.getKey()).size(); i++) {
			  bfw.write(toWriteToFile.get(i) + "\n");
		  }
		  bfw.close();
		  subLists.get(relation).get(thisEntry.getKey()).clear();
		}		
	}
	
	public void getnext() throws IOException {
		
		//	Output relation
		String outputRelation = relation1 + "_" + relation2 + "_join";
		BufferedWriter bfwOutput =  new BufferedWriter(new FileWriter(new File(outputRelation)));
		
		List <String> RFileList = subFileList.get(relation1);
		int count = 0;
		while(true) {
			if(count >= RFileList.size())
					break;
			//	Get the first file of R
			String RFile = RFileList.get(count);
			//	Get the corresponding file for the second relation
			String SFile = RFile.substring(0, RFile.indexOf("_")) + "_" + relation2;
			
			// System.out.println("RFile : " + RFile + " SFile : " + SFile);
			count++;
			if(!new File(SFile).exists())
				continue;
			
			BufferedReader bfrR = new BufferedReader(new FileReader(new File(RFile)));
			BufferedReader bfrS = new BufferedReader(new FileReader(new File(SFile)));
			
			String line = null;
			//	Get all the records of S in a list
			List <String> SRecords = new ArrayList<String>();
			while((line = bfrS.readLine()) != null) {
				SRecords.add(line);
			}
			bfrS.close();
			while((line = bfrR.readLine()) != null) {
				for(String record : SRecords) {
					// R.Y == S.Y condition
					// System.out.println(line + " :" + record);
					if(line.split(" ")[1].trim().equals(record.split(" ")[0].trim())){
						bfwOutput.write(line + " " + record.split(" ")[1].trim() + "\n");
					}
				}
			}
			bfrR.close();
		}
		bfwOutput.close();
	}
	
	private void close(BufferedReader bfr) throws IOException {
		bfr.close();
		// And delete all the file
		for(String file : subFileList.get(relation1)) {
			if(!new File(file).delete()) 
				System.err.println("Error in deleting the file : " + file);
		}
		for(String file : subFileList.get(relation2)) {
			if(!new File(file).delete()) 
				System.err.println("Error in deleting the file : " + file);
		}
	}
}
