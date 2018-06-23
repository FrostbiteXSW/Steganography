package _HexFileReader.XIONG;
import java.io.*;

public class HexFileReader{
	private File file;		// �����ļ���
	private int maxLine;	// ���δ�ӡ�����������跭ҳ
	
	// ���캯����ͨ�������ļ�λ�ô��ļ�
	public HexFileReader(String fileLocation) {
		try {
			file=new File(fileLocation);
			maxLine=500;
		} catch (Exception e) {
			System.out.println("Exception: "+e.toString());
		}
	}
	
	// ���õ��δ�ӡ����
	public int SetMaxLine(int maxLine) {
		int inbuf=this.maxLine;
		this.maxLine=maxLine;
		return inbuf;
	}
	
	// ��ӡ�ļ���������Ϣ
	public void PrintInHex() {
		try{
			System.out.printf("\nFile \"%s\" in Hex:\n",file.getPath());
			
			// ��ʼ�����������ļ���׼������
			InputStream in = new FileInputStream(file);
			int inbuf = 0,count = 0,line = 0,readbuf = 0;
			char[] tempstr = new char[16];
			
			// ����ļ�16������Ϣ��ASCII��ת������
			System.out.printf("%4d  ",++line);
			while((inbuf = in.read()) != -1){
				
				// ���16������Ϣ��һ�к����ASCII��ת�����ݲ�����
				if(++count > 16){
					System.out.printf("    ");
					for(int i=0;i<16;i++){
						if(i==8){
							System.out.printf("  ");
						}
						System.out.printf("%c",tempstr[i]);
					}
					System.out.println();
					
					// �ﵽ�����ʱѯ���Ƿ������ʾ
					if (line%maxLine==0) {
						System.out.printf("Show more? [Y/N]");
						
						// read()�������ջس����У�����ʱ��Ҫ����
						while ((readbuf=System.in.read())==13||readbuf==10) {
						}
						
						// �ж��������Y��y����������������ӡ
						if (readbuf!=89&&readbuf!=121) {
							System.out.println("Print stopped.");
							in.close();
							return;
						}
						
						System.out.println();
					}
					
					// �������п�ʼ��ӡ
					System.out.printf("%4d  ",++line);
					count = 1;
					tempstr = new char[16];
				}
				// ���8��16������Ϣ��տ����
				else if(count==9){
					System.out.printf("  ");
				}
				
				// �����ǰ16������Ϣ��ת����ASCII�������ݴ�
				System.out.printf("%02x ",inbuf);
				if(inbuf>=0&&inbuf<=32 || inbuf>=127){
					tempstr[count-1]='.';	// �����ܱ�ʾ�������õ��������ʾ
				}
				else{
					tempstr[count-1] = (char)inbuf;
				}
			}
			
			// �������һ�пո񲿷�
			for(int i=0;i<(count>8?52-3*count:54-3*count);i++){
				System.out.printf(" ");
			}
			
			// ������һ��ASCII��ת������
			for(int i=0;i<count;i++){
				if(i==8){
					System.out.printf("  ");
				}
				System.out.printf("%c",tempstr[i]);
			}
			
			// ������ӡ���ر��ļ���
			System.out.println("\nPrint finished.");
			in.close();
			
		} catch(Exception e){
			System.out.println("Exception: "+e.toString());
		}
	}
	
	// �������
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
