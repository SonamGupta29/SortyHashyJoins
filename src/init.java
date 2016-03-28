import java.io.IOException;

public class init {
	
	public static void main(String[] args) throws IOException {
		
		// Input format - ./a.out <path of R file> <path of S file> <sort/hash> <M>
		if(args.length != 4) {
			System.err.println("Invalid input !!!\nSyntax is : \"./a.out <path of R file> <path of S file> <sort/hash> <M>\"");
			System.exit(0);
		}
		Long startTime = System.currentTimeMillis();
		String R = args[0];
		String S = args[1];
		String joinType = args[2];
		int memoryBlocks = Integer.parseInt(args[3]);
		if(joinType.equalsIgnoreCase("hash")) {
			hashJoin h = new hashJoin();
			h.join(R, S, memoryBlocks);
		} else if(joinType.equalsIgnoreCase("sort")) {
			sortJoin s = new sortJoin();
		} else {
			System.err.println("Invalid type of join.");
		}
		Long endTime = System.currentTimeMillis();
		System.out.println("Join Completed...["+ (endTime - startTime) + "ms]");
	}
}