import java.io.IOException;
import _BitIOStream.BitInputStream;

public class TestMainClass {
	public static void main(String[] args) throws IOException {
		/*
		BitOutputStream bitOutputStream=new BitOutputStream("test.txt");
		for (int i = 0; i < 15; i++) {
			bitOutputStream.write(1);
			bitOutputStream.write(0);
		}
		int[] a= {0,0,0,1,1,0,0,1,1,0};
		bitOutputStream.write(a);
		bitOutputStream.write(a, 2, 4);
		bitOutputStream.close();\
		*/
		BitInputStream bitInputStream=new BitInputStream("test.txt");
		int count=0;
		while (bitInputStream.available()>0) {
			System.out.print(bitInputStream.read());
			if ((++count)==8) {
				System.out.print(" ");
				count=0;
			}
		}
		bitInputStream.close();
		
		System.out.println();
		
		bitInputStream=new BitInputStream("out.txt");
		count=0;
		while (bitInputStream.available()>0) {
			System.out.print(bitInputStream.read());
			if ((++count)==8) {
				System.out.print(" ");
				count=0;
			}
		}
		bitInputStream.close();
	}
}
