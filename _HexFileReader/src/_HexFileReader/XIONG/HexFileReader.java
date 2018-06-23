package _HexFileReader.XIONG;
import java.io.*;

public class HexFileReader{
	private File file;		// 对象文件流
	private int maxLine;	// 单次打印行数，超过需翻页
	
	// 构造函数，通过传入文件位置打开文件
	public HexFileReader(String fileLocation) {
		try {
			file=new File(fileLocation);
			maxLine=500;
		} catch (Exception e) {
			System.out.println("Exception: "+e.toString());
		}
	}
	
	// 设置单次打印行数
	public int SetMaxLine(int maxLine) {
		int inbuf=this.maxLine;
		this.maxLine=maxLine;
		return inbuf;
	}
	
	// 打印文件二进制信息
	public void PrintInHex() {
		try{
			System.out.printf("\nFile \"%s\" in Hex:\n",file.getPath());
			
			// 初始化变量，打开文件流准备读入
			InputStream in = new FileInputStream(file);
			int inbuf = 0,count = 0,line = 0,readbuf = 0;
			char[] tempstr = new char[16];
			
			// 输出文件16进制信息及ASCII码转换内容
			System.out.printf("%4d  ",++line);
			while((inbuf = in.read()) != -1){
				
				// 输出16进制信息满一行后输出ASCII码转换内容并换行
				if(++count > 16){
					System.out.printf("    ");
					for(int i=0;i<16;i++){
						if(i==8){
							System.out.printf("  ");
						}
						System.out.printf("%c",tempstr[i]);
					}
					System.out.println();
					
					// 达到最大行时询问是否继续显示
					if (line%maxLine==0) {
						System.out.printf("Show more? [Y/N]");
						
						// read()方法接收回车换行，遇到时需要过滤
						while ((readbuf=System.in.read())==13||readbuf==10) {
						}
						
						// 判断如果输入Y或y则继续，否则结束打印
						if (readbuf!=89&&readbuf!=121) {
							System.out.println("Print stopped.");
							in.close();
							return;
						}
						
						System.out.println();
					}
					
					// 另起新行开始打印
					System.out.printf("%4d  ",++line);
					count = 1;
					tempstr = new char[16];
				}
				// 输出8个16进制信息后空开间距
				else if(count==9){
					System.out.printf("  ");
				}
				
				// 输出当前16进制信息并转换成ASCII码内容暂存
				System.out.printf("%02x ",inbuf);
				if(inbuf>=0&&inbuf<=32 || inbuf>=127){
					tempstr[count-1]='.';	// 将不能表示的内容用点来代替表示
				}
				else{
					tempstr[count-1] = (char)inbuf;
				}
			}
			
			// 补足最后一行空格部分
			for(int i=0;i<(count>8?52-3*count:54-3*count);i++){
				System.out.printf(" ");
			}
			
			// 输出最后一行ASCII码转换内容
			for(int i=0;i<count;i++){
				if(i==8){
					System.out.printf("  ");
				}
				System.out.printf("%c",tempstr[i]);
			}
			
			// 结束打印，关闭文件流
			System.out.println("\nPrint finished.");
			in.close();
			
		} catch(Exception e){
			System.out.println("Exception: "+e.toString());
		}
	}
	
	// 调试入口
	public static void main(String args[]) {
		try {
			HexFileReader hexFileReader=new HexFileReader(args[args.length - 1]);
			for (int i = 0; i < args.length - 1; i++) {
				if (args[i].equalsIgnoreCase("-c")) {
					hexFileReader.SetMaxLine(Integer.parseInt(args[i + 1]));
					break;
				}
			}
			hexFileReader.PrintInHex();
		} catch (Exception e) {
			System.out.println("Exception: "+e.toString());
		}
	}
}
