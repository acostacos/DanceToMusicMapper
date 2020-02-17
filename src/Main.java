public class Main {
	public static void main(String[] args) {
		DanceMusicMapper dmm = new DanceMusicMapper();
		double[] test = dmm.MapDanceToMusic();
		System.out.println(test.length);
		for(int i=0; i<test.length; i++) {
			System.out.print(test[i]+" ");
		}
		System.out.println("");
	}
}
	